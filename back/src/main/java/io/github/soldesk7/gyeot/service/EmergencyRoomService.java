package io.github.soldesk7.gyeot.service;

import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import io.github.soldesk7.gyeot.client.EmergencyRoomClient;
import io.github.soldesk7.gyeot.dto.EgytListInfoResponse;
import io.github.soldesk7.gyeot.dto.EmergencyRoom;
import io.github.soldesk7.gyeot.dto.EmergencyRoomsResponse;
import io.github.soldesk7.gyeot.exception.HospitalDataUnavailableException;

/**
 * 응급실 조회의 도메인 로직 담당.
 *
 * 이 계층이 하는 일은 "경계 변환"이다. Client가 돌려주는 것은 공공데이터의 언어(EgytListInfoResponse.Item,
 * dutyName, wgs84Lat)이고, Controller가 프론트에 내보내야 하는 것은 우리 계약의 언어(EmergencyRoom,
 * name, lat)다. 그 사이를 연결해주는 계층이다. 덕분에 Controller는 공공데이터의 존재 자체를 알 필요 없으며 Client도
 * 우리 계약 DTO를 몰라도 된다.
 *
 * 추가로 해당 API가 해주지 않는 두 가지(거리 계산과 거리순 정렬)을 서비스 계층에서 실행한다.
 */
@Service
public class EmergencyRoomService {

    private static final Logger log = LoggerFactory.getLogger(EmergencyRoomService.class);

    /**
     * 받아둔 전국 목록과 그것을 받아온 시각을 함께 담은 레코드.
     *
     * 두 값을 한 객체로 묶는 이유: 목록과 시각을 별도로 관리하면 갱신 도중에 읽는 쪽이 "새 목록 + 옛 시각" 같은 어긋난 조합을
     * 볼 수 있다. 하나로 묶으면 참조 교체를 일회성으로 처리할 수 있어 일관성을 보장할 수 있다.
     */
    private record Snapshot(
            java.time.Instant fetchedAt,
            List<EgytListInfoResponse.Item> items
            ) {

    }

    /**
     * volatile: 갱신은 스케줄러 스레드가, 조회는 요청 처리 스레드가 실행한다. volatile이 없으면 한쪽이 바꾼 값이 다른
     * 쪽에 보이지 않을 수 있다. 레코드는 불변이고 교체가 참조 하나를 바꾸는 것이라, 읽는 쪽은 항상 완성된 스냅샷을 본다.
     */
    private volatile Snapshot snapshot;

    // 지도에 뿌릴 마커 수 상한. 전국 534곳을 모두 출력할 필요가 없기 때문
    private static final int NEARBY_LIMIT = 20;

    // 지구 평균 반지름(미터). 아래 거리 계산에 사용
    private static final int EARTH_RADIUS_M = 6_371_000;

    private final EmergencyRoomClient emergencyRoomClient;

    public EmergencyRoomService(EmergencyRoomClient emergencyRoomClient) {
        this.emergencyRoomClient = emergencyRoomClient;
    }

    /**
     * 좌표 기준으로 가까운 응급실 목록을 반환
     *
     * 처리 순서: 전국 목록을 받아 → 각 기관까지의 거리를 계산하고 → 가까운 순으로 정렬한 뒤 → 상위 20곳만 남긴다. 공공데이터가
     * 거리를 주지 않고 기관명 오름차순 정렬해 주기 때문에 이 과정이 필수
     *
     * 외부 호출이 실패하면(타임아웃·서버 오류·응답 구조 이상) Client가
     * HospitalDataUnavailableException으로 바꿔 던지고 GlobalExceptionHandler가 502
     * HOSPITAL_DATA_UNAVAILABLE로 응답한다.
     *
     * @param lat 위도(WGS84)
     * @param lng 경도(WGS84)
     * @return 기준 시각(asOf)과 응급실 목록을 담은 계약 응답
     */
    public EmergencyRoomsResponse findNearby(double lat, double lng) {
        Snapshot current = snapshot;
        if (current == null) {
            // 기동 직후 예약 갱신이 아직 끝나지 않았거나 실패한 경우.
            // 여기서 조회가 실패하면 예외가 그대로 올라가 502 계약 응답이 된다.
            current = fetch();
            snapshot = current;
        }

        List<EmergencyRoom> items = current.items().stream()
                /* 
                공공데이터에 좌표가 비어 있는 기관이 섞여 있다. 
                지도에 표시할 수 없고 거리 계산도 불가능하므로 제외한다. 
                (0.0으로 채우면 위경도 (0,0) = 아프리카 앞바다로 계산돼 정렬이 망가지므로 기본값 대체는 배제한다) 
                 */
                .filter(item -> item.wgs84Lat() != null && item.wgs84Lon() != null)
                .map(item -> toEmergencyRoom(item, lat, lng))
                .sorted(Comparator.comparingInt(EmergencyRoom::distanceM))
                .limit(NEARBY_LIMIT)
                .toList();

        // asOf: 응급실 목록을 공공데이터에서 받아온 시각. 요청 시각이 아니다.
        // 프론트엔드에서 "이 정보가 얼마나 오래된 정보인지"를 알아야 하기 때문
        return new EmergencyRoomsResponse(current.fetchedAt(), items);
    }

    /** 공공데이터에서 전국 응급실 목록을 새로 받아 스냅샷으로 만든다. 실패하면 예외가 그대로 올라간다. */
    private Snapshot fetch() {
        // List.copyOf: 여러 요청 스레드가 동시에 읽으므로 불변 목록으로 고정.
        return new Snapshot(java.time.Instant.now(), List.copyOf(emergencyRoomClient.findAll()));
    }

    /**
     * 공공데이터 항목 하나를 우리 계약 DTO로 변환한다.
     */
    private static EmergencyRoom toEmergencyRoom(EgytListInfoResponse.Item item, double lat, double lng) {
        return new EmergencyRoom(
                item.dutyName(),
                item.wgs84Lat(),
                item.wgs84Lon(),
                null, // 가용병상: 다른 오퍼레이션(주소 기준)에 있어 2단계 조합이 필요하다. 캐싱과 함께 SP3에서.
                distanceMeters(lat, lng, item.wgs84Lat(), item.wgs84Lon())
        );
    }

    /**
     * 두 좌표 사이의 직선거리를 미터로 계산한다(하버사인 공식).
     *
     * 위경도는 평면 좌표가 아니라 구체 위의 각도 - 단순 뺄셈 후 피타고라스 공식으로 계산하면 오차가 크다. 하버사인: 지구를 구로
     * 보고 두 지점 사이의 최단 거리(대권 거리)를 구하는 표준 공식 실제 도로 거리가 아니라 직선거리 -> 화면에서는 "가까운 순"을
     * 정하는 기준으로만 쓴다. 차후에는 도로 거리 API를 호출해 실제로 걸리는 시간을 기준으로 정렬하는 방법도 고려할 수 있다.
     */
    private static int distanceMeters(double lat1, double lng1, double lat2, double lng2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);

        return (int) Math.round(EARTH_RADIUS_M * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a)));
    }

    /**
     * 전국 목록을 주기적으로 미리 받아두는 스케줄러.
     *
     * 사용자 요청이 들어온 뒤에 조회하면 그 사용자가 응답을 기다려야 한다.(실측결과 약 7초 소요).
     * 미리 받아두면 서버 측 메모리에서 바로 조회 가능하다.
     *
     * 갱신에 실패해도 기존 스냅샷을 지우지 않는다. 
     * 목록이 비면 지도에 응급실 목록이 아예 출력되지 않는디.
     * 몇 시간 지난 목록이라도 빈 화면보다 낫다. (응급실 위치는 자주 바뀌지 않는다)
     *
     * fixedDelay는 첫 실행이 기동 직후에 일어나고 이후에는 직전 실행이 끝난 시점부터 주기를 잰다.
     * 조회에 7초가 걸려도 실행이 겹치지 않는다.
     */
    @Scheduled(fixedDelayString = "${gyeot.emergency-room.refresh-ms}")
    void refresh() {
        try {
            snapshot = fetch();
        } catch (HospitalDataUnavailableException e) {
            log.warn("응급실 목록 갱신 실패 — 기존 목록을 유지한다", e);
        }
    }

    /**
     * 병상 조회에 넘길 행정구역. 목록의 기관 주소에서 잘라낸다.
     *
     * 공공데이터의 병상 오퍼레이션이 시도(STAGE1)·시군구(STAGE2)를 파라미터로 받으므로 이 두 값이 곧 어느 시군구의 병상을 받아왔는지를 가리키는 캐시 키가 된다.
     * 레코드는 equals·hashCode가 자동으로 만들어져 Map의 키로 그대로 쓸 수 있다.
     */
    record District(
            String sido, 
            String sigungu
            ) {
    }

    /**
     * 기관 주소에서 시도·시군구를 잘라낸다.
     *
     * 주소는 "울산광역시 남구 남산로354번길 26 (신정동)"처럼 공백으로 나뉜 문자열로 온다.
     * 첫 토큰이 시도, 둘째 토큰이 시군구다.
     *
     * 둘째 토큰이 시·군·구로 끝나지 않으면 시군구 단계가 없는 주소다. 
     * 세종특별자치시가 여기 해당하며 ("세종특별자치시 보듬7로 20") 이때는 시군구를 빈 값으로 두어 시도 전체를 조회한다.
     * 공공데이터 문서에는 시군구가 필수로 표기돼 있으나 빈 값도 정상 동작하는 것을 실측으로 확인했다.
     *
     * "경기도 성남시 분당구"처럼 세 단계인 주소는 둘째 토큰인 "성남시"로 잡힌다. 
     * 분당구보다 넓은 범위로 조회되므로 분당구 기관이 결과에 포함된다 — 누락이 생기지 않는 방향이다.
     *
     * @return 주소가 비어 있으면 null. 조회 대상에서 제외한다.
     */
    static District parseDistrict(String dutyAddr) {
        if (dutyAddr == null || dutyAddr.isBlank()) {
            return null;
        }
        String[] tokens = dutyAddr.trim().split("\\s+");
        String sigungu = (tokens.length > 1 && isSigungu(tokens[1])) ? tokens[1] : "";
        return new District(tokens[0], sigungu);
    }

    /** 시군구 이름인지 판별한다. 도로명·법정동은 로·길·동 등으로 끝나 여기서 걸러진다. */
    private static boolean isSigungu(String token) {
        return token.endsWith("시") || token.endsWith("군") || token.endsWith("구");
    }
}
