package io.github.soldesk7.gyeot.service;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.soldesk7.gyeot.client.EmergencyRoomClient;
import io.github.soldesk7.gyeot.dto.EgytListInfoResponse;
import io.github.soldesk7.gyeot.dto.EmergencyRoomsResponse;
import io.github.soldesk7.gyeot.dto.EmrrmRltmUsefulSckbdInfoResponse;
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

    /** 테스트가 도는 동안 병상 캐시가 만료되지 않도록 넉넉히 잡은 값. */
    private static final long TTL_KEEP = 60_000;

    /** 캐시가 만료된 상태로 테스트 하기 위한 값. */
    private static final long TTL_EXPIRE = 0;

    /**
     * 실측 응답(서울 강남구)에서 가져온 병상 현황에 병상 값이 없는 기관을 하나 더한 것.
     * 0은 만실, 음수는 정원 초과를 뜻한다.
     */
    private static final List<EmrrmRltmUsefulSckbdInfoResponse.Item> BEDS = List.of(
            new EmrrmRltmUsefulSckbdInfoResponse.Item("A1100015", 0, "20260730164621"),
            new EmrrmRltmUsefulSckbdInfoResponse.Item("A1100010", -16, "20260730164713"),
            new EmrrmRltmUsefulSckbdInfoResponse.Item("A9999999", null, null));

    private static final EmergencyRoomService.District 강남구 =
            new EmergencyRoomService.District("서울특별시", "강남구");

    /** 좌표가 있는 최소 항목. 거리 계산 결과 자체는 이 테스트에서 중요하지 않다. */
    private static final List<EgytListInfoResponse.Item> ITEMS = List.of(
            new EgytListInfoResponse.Item("삼성서울병원", "서울특별시 강남구 일원로 81 (일원동)", "A0000028", 37.4881326, 127.0851566));

    @Test
    void 두_번째_조회는_외부_API를_다시_부르지_않는다() {
        EmergencyRoomClient client = mock(EmergencyRoomClient.class);
        when(client.findAll()).thenReturn(ITEMS);
        EmergencyRoomService service = new EmergencyRoomService(client, 180000);

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
        EmergencyRoomService service = new EmergencyRoomService(client, 1000);

        EmergencyRoomsResponse before = service.findNearby(37.5, 127.0);

        // 다음 갱신은 실패하도록 바꾼다 (공공데이터 타임아웃 상황)
        when(client.findAll()).thenThrow(new HospitalDataUnavailableException(new RuntimeException("타임아웃")));
        service.refresh();

        EmergencyRoomsResponse after = service.findNearby(37.5, 127.0);

        assertEquals(before.asOf(), after.asOf());   // 이전 스냅샷이 그대로 남았다
        assertEquals(1, after.items().size());
    }
    
    /**
     * 주소 문자열에서 병상 조회에 넘길 시도·시군구를 잘라내는 규칙을 고정한다.
     *
     * 이 규칙이 어긋나면 병상 조회가 조용히 0건을 돌려주고 화면에는 "병상 정보 없음"으로만 보인다.
     * 오류가 나지 않아 눈치채기 어려우므로 실제 응답에서 가져온 주소 형태별로 결과를 못박아 둔다.
     */
    @Test
    void 주소에서_시도와_시군구를_잘라낸다() {
        // 가장 흔한 형태 — 시도 + 시군구
        EmergencyRoomService.District basic =
                EmergencyRoomService.parseDistrict("울산광역시 남구 남산로354번길 26 (신정동)");
        assertEquals("울산광역시", basic.sido());
        assertEquals("남구", basic.sigungu());

        // 군 단위도 같은 규칙으로 잡힌다
        EmergencyRoomService.District county =
                EmergencyRoomService.parseDistrict("전남광주통합특별시 화순군 화순읍 서양로 322");
        assertEquals("전남광주통합특별시", county.sido());
        assertEquals("화순군", county.sigungu());
    }

    @Test
    void 세_단계_주소는_시까지만_잘라낸다() {
        // "성남시 분당구"처럼 세 단계인 주소는 둘째 토큰인 시에서 멈춘다.
        // 분당구보다 넓은 범위로 조회되므로 분당구 기관이 결과에 포함된다 — 누락이 생기지 않는 방향이다.
        EmergencyRoomService.District d =
                EmergencyRoomService.parseDistrict("경기도 성남시 분당구 야탑로 59");
        assertEquals("경기도", d.sido());
        assertEquals("성남시", d.sigungu());
    }

    @Test
    void 시군구_단계가_없으면_시군구를_비운다() {
        // 세종특별자치시는 시군구 단계가 없어 둘째 토큰이 도로명이다.
        // 시군구를 빈 값으로 두면 시도 전체가 조회된다(공공데이터 문서에는 필수로 표기돼 있으나 실제로는 빈 값도 동작).
        // 도로명을 그대로 넘기면 그 도로의 기관만 나와 나머지가 누락된다.
        EmergencyRoomService.District d =
                EmergencyRoomService.parseDistrict("세종특별자치시 보듬7로 20, 세종충남대학교병원 (도담동)");
        assertEquals("세종특별자치시", d.sido());
        assertEquals("", d.sigungu());
    }

    @Test
    void 주소가_없으면_null을_반환한다() {
        // 실측 534건에는 모두 주소가 있었지만, 좌표가 비어 있는 기관이 섞여 있던 전례가 있어 방어한다.
        // 이 경우 병상 조회 대상에서 제외한다.
        assertNull(EmergencyRoomService.parseDistrict(null));
        assertNull(EmergencyRoomService.parseDistrict("   "));
    }

    /**
     * 병상 캐시가 동작하는지 검증한다.
     *
     * 캐시가 없어도 화면 결과는 같아서 눈으로는 구분되지 않는다. 
     * 다만 요청마다 공공데이터를 부르게 되고 병상은 시군구 단위라 호출 수가 사용자 행동에 비례해 늘어난다. 
     * 외부 호출 횟수로 확인한다.
     */
    @Test
    void 같은_시군구를_다시_조회하면_외부_API를_부르지_않는다() {
        EmergencyRoomClient client = mock(EmergencyRoomClient.class);
        when(client.findBeds("서울특별시", "강남구")).thenReturn(BEDS);
        EmergencyRoomService service = new EmergencyRoomService(client, TTL_KEEP);

        assertEquals(0, service.bedsOf(강남구).get("A1100015"));    // 만실
        assertEquals(-16, service.bedsOf(강남구).get("A1100010"));  // 16명 초과

        // 병상 값이 없는 기관은 담지 않는다 — 매칭 단계에서 availableBeds가 null이 된다.
        assertFalse(service.bedsOf(강남구).containsKey("A9999999"));

        verify(client, times(1)).findBeds("서울특별시", "강남구");
    }

    @Test
    void 캐시가_만료되면_다시_조회한다() {
        EmergencyRoomClient client = mock(EmergencyRoomClient.class);
        when(client.findBeds("서울특별시", "강남구")).thenReturn(BEDS);
        EmergencyRoomService service = new EmergencyRoomService(client, TTL_EXPIRE);

        service.bedsOf(강남구);
        service.bedsOf(강남구);

        verify(client, times(2)).findBeds("서울특별시", "강남구");
    }

    /**
     * 캐시가 동작하지 않아도 API 응답 내용은 똑같이 나오므로 응답만 봐서는 캐시 여부를 구분할 수 없다. 
     * 그래서 공공데이터 호출 횟수를 세는 방식으로 확인한다.
     *
     * 실패를 캐시에 담지 않으면 공공데이터 장애가 이어지는 동안 사용자 요청 수만큼 조회가 반복되어 일일 트래픽 사용량이 급격히 증가한다.
     */
    @Test
    void 병상_조회에_실패하면_빈_결과를_돌려주고_다시_부르지_않는다() {
        EmergencyRoomClient client = mock(EmergencyRoomClient.class);
        when(client.findBeds("서울특별시", "강남구"))
                .thenThrow(new HospitalDataUnavailableException(new RuntimeException("트래픽 한도 초과")));
        EmergencyRoomService service = new EmergencyRoomService(client, TTL_KEEP);

        assertTrue(service.bedsOf(강남구).isEmpty());
        assertTrue(service.bedsOf(강남구).isEmpty());

        verify(client, times(1)).findBeds("서울특별시", "강남구");
    }
}