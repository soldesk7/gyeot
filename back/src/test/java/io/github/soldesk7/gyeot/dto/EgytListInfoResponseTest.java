package io.github.soldesk7.gyeot.dto;

import org.junit.jupiter.api.Test;
import tools.jackson.dataformat.xml.XmlMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * EgytListInfoResponse의 XML 매핑 선언이 실제 공공데이터 응답과 맞는지 검증한다.
 *
 * 왜 이런 테스트가 필요한가: Jackson 매핑은 애너테이션으로 선언만 할 뿐이라, 잘못 써도 컴파일 단계에서는 오류 로그가 출력되지
 * 않는다. 실제로 XML을 파싱하는 런타임에 가서야 터지는데 서버를 띄우고 외부 API를 호출한 뒤에야 알게 되면 원인을 찾기가 힘들 것으로
 * 예상된다. 그래서 XML 문자열 하나로 매핑만 따로 검증한다. (이 이전 EgytLcinfoResponseTest 테스트로 record에서
 * @JacksonXmlElementWrapper가 동작하지 않는 Jackson 버그를 미리 발견했다)
 */
class EgytListInfoResponseTest {

    /**
     * 실제 공공데이터 응답을 줄인 것(기관 3곳).
     *
     * hpid·phpid·rnum·dutyEmcls 등 우리가 선언하지 않은 필드를 일부러 남겨뒀다 —
     *
     * @JsonIgnoreProperties(ignoreUnknown = true)가 제대로 걸려 있어야 이 필드들을 무시하고 통과한다.
     * 누락되어 있으면 여기서 파싱이 실패하므로, 이 XML 자체가 그 설정에 대한 검증이다.
     */
    private static final String XML = """
            <response>
                <header><resultCode>00</resultCode><resultMsg>NORMAL SERVICE.</resultMsg></header>
                <body>
                    <items>
                        <item>
                            <dutyAddr>울산광역시 남구 남산로354번길 26 (신정동)</dutyAddr>
                            <dutyEmcls>G009</dutyEmcls>
                            <dutyEmclsName>응급실운영신고기관</dutyEmclsName>
                            <dutyName>(의)내경의료재단울산제일병원</dutyName>
                            <dutyTel3>052-220-3334</dutyTel3>
                            <hpid>A1700023</hpid>
                            <rnum>1</rnum>
                            <wgs84Lat>35.54823820112527</wgs84Lat>
                            <wgs84Lon>129.30701143429678</wgs84Lon>
                        </item>
                        <item>
                            <dutyAddr>부산광역시 기장군 기장읍 대청로72번길 6</dutyAddr>
                            <dutyEmcls>G007</dutyEmcls>
                            <dutyEmclsName>지역응급의료기관</dutyEmclsName>
                            <dutyName>(의)서일의료재단기장병원</dutyName>
                            <dutyTel3>051-723-2119</dutyTel3>
                            <hpid>A1200028</hpid>
                            <rnum>2</rnum>
                            <wgs84Lat>35.23602946449906</wgs84Lat>
                            <wgs84Lon>129.21649161387128</wgs84Lon>
                        </item>
                        <item>
                            <dutyName>좌표없는기관</dutyName>
                            <hpid>A9999999</hpid>
                        </item>
                    </items>
                    <numOfRows>3</numOfRows>
                    <pageNo>1</pageNo>
                    <totalCount>534</totalCount>
                </body>
            </response>
            """;

    @Test
    void XML_응답을_매핑한다() {
        // XmlMapper: XML을 자바 객체로 바꿔주는 Jackson의 XML 전용 매퍼.
        // 실제 Client도 이와 같은 방식으로 응답을 파싱할 예정.
        EgytListInfoResponse response = new XmlMapper().readValue(XML, EgytListInfoResponse.class);

        // header 안의 값이 한 단계 아래 타입(Header)으로 잘 들어갔는지 — 중첩 구조 매핑 확인
        assertEquals("00", response.header().resultCode());

        // items 껍데기 안의 item 3개가 List로 모였는지 — @JacksonXmlElementWrapper 동작 확인
        assertEquals(3, response.body().items().size());

        // 순서 보존 확인 (가나다순 오름차순)
        assertEquals("(의)서일의료재단기장병원", response.body().items().get(1).dutyName());

        // 문자열로 온 XML 값이 double로 변환됐는지 — XML은 타입이 없어 전부 문자열로 오므로, Jackson이 타입 변환을 잘 해주는지 확인
        assertEquals(35.54823820112527, response.body().items().get(0).wgs84Lat());

        // 좌표가 비어 있는 기관도 예외 없이 매핑되는지 — 실제 공공데이터에 결측이 섞여 있다.
        // 필드가 double(원시 타입)이면 여기서 "Cannot map null into type double"로 파싱이 통째로 실패한다. 
        // Double(래퍼)을 유지해야 하는 이유이며 Double 래퍼 클레스를 사용한 이유를 잊어 double로 되돌린다면 이 테스트로 잡을 수 있다.
        assertNull(response.body().items().get(2).wgs84Lat());
    }
}
