package io.github.soldesk7.gyeot.service;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.soldesk7.gyeot.client.EmergencyRoomClient;
import io.github.soldesk7.gyeot.dto.EgytListInfoResponse;
import io.github.soldesk7.gyeot.dto.EmergencyRoomsResponse;
import io.github.soldesk7.gyeot.exception.HospitalDataUnavailableException;

/**
 * 전국 목록을 한 번 받아 재사용하는지 검증한다.
 *
 * 테스트가 필요한 이유: 캐시가 동작하지 않아도 응답 내용은에 차이가 발생하지는 않기 때문
 * — 매번 외부 API를 부르며 느려질 뿐이라 화면만 봐서는 구분되지 않는다. 
 * 그래서 호출 결과가 아니라 외부 호출 횟수로 확인한다.
 *
 * Client를 목으로 대체하므로 네트워크를 거치지 않는다.
 */
class EmergencyRoomServiceTest {

    /** 좌표가 있는 최소 항목. 거리 계산 결과 자체는 이 테스트에서 중요하지 않다. */
    private static final List<EgytListInfoResponse.Item> ITEMS = List.of(
            new EgytListInfoResponse.Item("삼성서울병원", "서울특별시 강남구 일원로 81 (일원동)", "A0000028", 37.4881326, 127.0851566));

    @Test
    void 두_번째_조회는_외부_API를_다시_부르지_않는다() {
        EmergencyRoomClient client = mock(EmergencyRoomClient.class);
        when(client.findAll()).thenReturn(ITEMS);
        EmergencyRoomService service = new EmergencyRoomService(client);

        EmergencyRoomsResponse first = service.findNearby(37.5, 127.0);
        EmergencyRoomsResponse second = service.findNearby(35.2, 129.2);  // 좌표를 바꿔도 목록은 재사용

        verify(client, times(1)).findAll();

        // 같은 스냅샷을 썼다면 데이터를 받아온 시각도 같다.
        assertEquals(first.asOf(), second.asOf());
    }

    @Test
    void 갱신에_실패해도_기존_목록으로_응답한다() {
        EmergencyRoomClient client = mock(EmergencyRoomClient.class);
        when(client.findAll()).thenReturn(ITEMS);
        EmergencyRoomService service = new EmergencyRoomService(client);

        EmergencyRoomsResponse before = service.findNearby(37.5, 127.0);

        // 다음 갱신은 실패하도록 바꾼다 (공공데이터 타임아웃 상황)
        when(client.findAll()).thenThrow(new HospitalDataUnavailableException(new RuntimeException("타임아웃")));
        service.refresh();

        EmergencyRoomsResponse after = service.findNearby(37.5, 127.0);

        assertEquals(before.asOf(), after.asOf());   // 이전 스냅샷이 그대로 남았다
        assertEquals(1, after.items().size());
    }
}