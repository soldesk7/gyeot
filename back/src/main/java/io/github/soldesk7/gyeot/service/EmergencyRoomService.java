package io.github.soldesk7.gyeot.service;

import io.github.soldesk7.gyeot.client.EmergencyRoomClient;
import io.github.soldesk7.gyeot.dto.EgytLcinfoResponse;
import io.github.soldesk7.gyeot.dto.EmergencyRoom;
import io.github.soldesk7.gyeot.dto.EmergencyRoomsResponse;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

/**
 * 응급실 조회의 도메인 로직 담당.
 *
 * 이 계층이 하는 일은 "경계 변환"이다. Client가 돌려주는 것은 공공데이터의 언어(EgytLcinfoResponse.Item,
 * dutyName, km 단위 거리)이고, Controller가 프론트에 내보내야 하는 것은 우리 계약의 언어 (EmergencyRoom,
 * name, 미터 단위 거리)다. 그 사이를 연결해주는 계층이다.
 *
 * 덕분에 Controller는 공공데이터의 존재 자체를 알 필요 없고, Client도 우리 계약 DTO를 몰라도 된다.
 */
@Service
public class EmergencyRoomService {

    private final EmergencyRoomClient emergencyRoomClient;

    public EmergencyRoomService(EmergencyRoomClient emergencyRoomClient) {
        this.emergencyRoomClient = emergencyRoomClient;
    }

    /**
     * 좌표 기준으로 인근 응급실 목록을 조회
     *
     * 결과는 가까운 순으로 정렬돼 있다 — 공공데이터가 거리순으로 돌려주고, 아래 stream().map()은 순서를 그대로 보존하므로 우리가 재정렬할 필요가 없다. 
     * 개수 상한(20건)은 Client가 요청 단계에서 설정한다.
     *
     * 외부 호출이 실패하면(타임아웃·서버 오류) 예외가 그대로 올라간다. 
     * 이를 502 HOSPITAL_DATA_UNAVAILABLE 같은 계약 응답으로 바꾸는 일은 이슈 #19에서 두 API(F-09·F-05)에 일관되게 적용할 예정
     *
     * @param lat 위도(WGS84)
     * @param lng 경도(WGS84)
     * @return 기준 시각(asOf)과 응급실 목록을 담은 계약 응답
     */
    public EmergencyRoomsResponse findNearby(double lat, double lng) {
        List<EmergencyRoom> items = emergencyRoomClient.findNearby(lat, lng).stream()
                .map(EmergencyRoomService::toEmergencyRoom)
                .toList();

        // asOf: 데이터 기준 시각. SP2에서는 방금 조회한 시각을 그대로 쓴다.
        // TODO: SP3에서 캐싱을 개발하면 "캐시에 담긴 데이터를 언제 받아왔는지"로 의미 변경
        return new EmergencyRoomsResponse(Instant.now(), items);
    }

    /** 공공데이터 항목 하나를 우리 계약 DTO로 변환한다. */
    private static EmergencyRoom toEmergencyRoom(EgytLcinfoResponse.Item item) {
        return new EmergencyRoom(
                item.dutyName(),
                item.latitude(),
                item.longitude(),
                null, // 가용병상: 다른 오퍼레이션(주소 기준)에 있어 2단계 조합이 필요하다. 캐싱과 함께 SP3에서 실제 값 삽입 예정
                toMeters(item.distance())
        );
    }

    /** 공공데이터는 거리를 km(예: 3.47)로 주는데, 계약은 미터 단위 정수를 사용하므로 환산하는 과정이 필요하다. */
    private static int toMeters(double distanceKm) {
        return (int) Math.round(distanceKm * 1000);
    }
}
