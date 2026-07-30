package io.github.soldesk7.gyeot.service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;

import io.github.soldesk7.gyeot.client.EmergencyRoomClient;
import io.github.soldesk7.gyeot.dto.EgytListInfoResponse;
import io.github.soldesk7.gyeot.dto.EmergencyRoom;
import io.github.soldesk7.gyeot.dto.EmergencyRoomsResponse;

/**
 * 응급실 조회의 도메인 로직 담당.
 *
 * 이 계층이 하는 일은 "경계 변환"이다. 
 * Client가 돌려주는 것은 공공데이터의 언어(EgytListInfoResponse.Item, dutyName, wgs84Lat)이고, 
 * Controller가 프론트에 내보내야 하는 것은 우리 계약의 언어(EmergencyRoom, name, lat)다.
 * 그 사이를 연결해주는 계층이다.
 * 덕분에 Controller는 공공데이터의 존재 자체를 알 필요 없으며 Client도 우리 계약 DTO를 몰라도 된다.
 * 
 * 추가로 해당 API가 해주지 않는 두 가지(거리 계산과 거리순 정렬)을 서비스 계층에서 실행한다.
 */
@Service
public class EmergencyRoomService {

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
     * 처리 순서: 전국 목록을 받아 → 각 기관까지의 거리를 계산하고 → 가까운 순으로 정렬한 뒤 → 상위 20곳만 남긴다. 
     * 공공데이터가 거리를 주지 않고 기관명 오름차순 정렬해 주기 때문에 이 과정이 필수
     *
     * 외부 호출이 실패하면(타임아웃·서버 오류·응답 구조 이상) Client가 HospitalDataUnavailableException으로 바꿔 던지고
     * GlobalExceptionHandler가 502 HOSPITAL_DATA_UNAVAILABLE로 응답한다.
     *
     * @param lat 위도(WGS84)
     * @param lng 경도(WGS84)
     * @return 기준 시각(asOf)과 응급실 목록을 담은 계약 응답
     */
    public EmergencyRoomsResponse findNearby(double lat, double lng) {
        List<EmergencyRoom> items = emergencyRoomClient.findAll().stream()
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

        // asOf: 데이터 기준 시각. SP2에서는 방금 조회한 시각을 그대로 쓴다.
        // TODO: SP3에서 캐싱을 개발하면 "캐시에 담긴 데이터를 언제 받아왔는지"로 의미 변경
        return new EmergencyRoomsResponse(Instant.now(), items);
    }

    /** 공공데이터 항목 하나를 우리 계약 DTO로 변환한다. */
    private static EmergencyRoom toEmergencyRoom(EgytListInfoResponse.Item item, double lat, double lng) {
        return new EmergencyRoom(
                item.dutyName(),
                item.wgs84Lat(),
                item.wgs84Lon(),
                null,   // 가용병상: 다른 오퍼레이션(주소 기준)에 있어 2단계 조합이 필요하다. 캐싱과 함께 SP3에서.
                distanceMeters(lat, lng, item.wgs84Lat(), item.wgs84Lon())
        );
    }

    /**
     * 두 좌표 사이의 직선거리를 미터로 계산한다(하버사인 공식).
     *
     * 위경도는 평면 좌표가 아니라 구체 위의 각도 - 단순 뺄셈 후 피타고라스 공식으로 계산하면 오차가 크다.
     * 하버사인: 지구를 구로 보고 두 지점 사이의 최단 거리(대권 거리)를 구하는 표준 공식
     * 실제 도로 거리가 아니라 직선거리 -> 화면에서는 "가까운 순"을 정하는 기준으로만 쓴다. 
     * 차후에는 도로 거리 API를 호출해 실제로 걸리는 시간을 기준으로 정렬하는 방법도 고려할 수 있다.
     */
    private static int distanceMeters(double lat1, double lng1, double lat2, double lng2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);

        return (int) Math.round(EARTH_RADIUS_M * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a)));
    }
}
