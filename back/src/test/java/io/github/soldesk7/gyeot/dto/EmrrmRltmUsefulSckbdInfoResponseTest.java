package io.github.soldesk7.gyeot.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;

import tools.jackson.dataformat.xml.XmlMapper;

/**
 * EmrrmRltmUsefulSckbdInfoResponse의 XML 매핑 선언이 실제 공공데이터 응답과 맞는지 검증한다.
 *
 * 왜 이런 테스트가 필요한가: Jackson 매핑은 애너테이션으로 선언만 할 뿐이라 잘못 써도 컴파일 단계에서는 드러나지 않는다.
 * 실제로 XML을 파싱하는 런타임에 가서야 터지므로, XML 문자열 하나로 매핑만 따로 검증한다.
 *
 * 이 응답은 필드가 60개가 넘어(병상 종류별 수치·장비 가용 여부·당직의 연락처 등) 우리가 선언하지 않은 필드를 무시하는 설정이 특히 중요하다.
 */
class EmrrmRltmUsefulSckbdInfoResponseTest {

    /**
     * 실제 응답(서울특별시 강남구, 2026-07-30 조회)을 줄인 것에 병상 결측 항목 하나를 더한 것.
     *
     * dutyName·phpid·rnum·hvs01·hvctayn 등 우리가 선언하지 않은 필드를 일부러 남겨뒀다 -
     * @JsonIgnoreProperties(ignoreUnknown = true)가 제대로 걸려 있어야 이 필드들을 무시하고 통과한다.
     */
    private static final String XML = """
            <response>
                <header><resultCode>00</resultCode><resultMsg>NORMAL SERVICE.</resultMsg></header>
                <body>
                    <items>
                        <item>
                            <dutyName>연세대학교의과대학강남세브란스병원</dutyName>
                            <hpid>A1100015</hpid>
                            <phpid>A1100015</phpid>
                            <rnum>1</rnum>
                            <hvec>0</hvec>
                            <hvidate>20260730164621</hvidate>
                            <hvs01>21</hvs01>
                            <hvctayn>Y</hvctayn>
                        </item>
                        <item>
                            <dutyName>삼성서울병원</dutyName>
                            <hpid>A1100010</hpid>
                            <phpid>A1100010</phpid>
                            <rnum>2</rnum>
                            <hvec>-16</hvec>
                            <hvidate>20260730164713</hvidate>
                            <hvs01>52</hvs01>
                            <hvctayn>Y</hvctayn>
                        </item>
                        <item>
                            <dutyName>병상정보없는기관</dutyName>
                            <hpid>A9999999</hpid>
                            <rnum>3</rnum>
                        </item>
                    </items>
                    <numOfRows>100</numOfRows>
                    <pageNo>1</pageNo>
                    <totalCount>3</totalCount>
                </body>
            </response>
            """;

    @Test
    void XML_응답을_매핑한다() {
        EmrrmRltmUsefulSckbdInfoResponse response =
                new XmlMapper().readValue(XML, EmrrmRltmUsefulSckbdInfoResponse.class);

        // header 안의 값이 한 단계 아래 타입(Header)으로 들어갔는지 — 중첩 구조 매핑 확인
        assertEquals("00", response.header().resultCode());

        // items 껍데기 안의 item 3개가 List로 모였는지 — @JacksonXmlElementWrapper 동작 확인
        assertEquals(3, response.body().items().size());

        // 목록 응답과 짝지을 때 쓰는 키
        assertEquals("A1100010", response.body().items().get(1).hpid());

        // 0은 만실을 뜻하는 정상 값이다. 아래 결측(null)과 구분돼야 하므로 따로 확인한다.
        assertEquals(0, response.body().items().get(0).hvec());

        // 음수는 정원 초과 인원이다. 오류 값이 아니므로 부호를 잃지 않고 그대로 들어와야 한다.
        assertEquals(-16, response.body().items().get(1).hvec());

        // 병상 값이 없는 기관도 예외 없이 매핑되는지.
        // 필드가 int(원시 타입)면 여기서 "Cannot map null into type int"로 파싱이 통째로 실패하고 기본값 0으로 대체하면 만실(0)과 정보 없음을 구분할 수 없게 된다.
        // Integer(래퍼)를 유지해야 하는 이유다.
        assertNull(response.body().items().get(2).hvec());

        // 갱신 시각은 벤더 형식(14자리 문자열) 그대로 받는다 — 시각 타입 변환은 서비스 계층의 몫이다.
        assertEquals("20260730164713", response.body().items().get(1).hvidate());
    }
}