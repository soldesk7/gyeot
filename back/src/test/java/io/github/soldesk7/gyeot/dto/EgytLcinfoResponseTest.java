package io.github.soldesk7.gyeot.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

import tools.jackson.dataformat.xml.XmlMapper;

/**
 * EgytLcinfoResponse의 XML 매핑 선언이 실제 공공데이터 응답과 맞는지 검증
 *
 * 왜 이런 테스트가 필요한가:
 * Jackson 매핑은 애너테이션으로 선언만 할 뿐이라, 잘못 써도 컴파일 단계에서는 오류 로그가 출력되지 않는다.
 * 실제로 XML을 파싱하는 런타임에 가서야 터지는데 서버를 띄우고 외부 API를 호출한 뒤에야 알게 되면 원인을 찾기가 힘들 것으로 예상된다.
 * 그래서 XML 문자열 하나로 매핑만 따로 검증한다.
 * (이 테스트로 record에서 @JacksonXmlElementWrapper가 동작하지 않는 Jackson 버그를 미리 발견했다)
 *
 * 외부 API를 호출하지 않으므로 네트워크·인증키 없이 CI에서도 정상 작동한다.
 */
class EgytLcinfoResponseTest {

    /**
     * 실제 공공데이터 응답 요약(병원 2건).
     *
     * hpid와 numOfRows를 일부러 남겨뒀다 — 우리가 DTO에 선언하지 않은 필드다.
     * @JsonIgnoreProperties(ignoreUnknown = true)가 제대로 걸려 있어야 이 필드들을
     * 무시하고 통과한다. 빠져 있으면 여기서 파싱이 실패하므로, 이 XML 자체가 그 설정에 대한 검증이다.
     */
    private static final String XML = """
            <response>
              <header><resultCode>00</resultCode><resultMsg>NORMAL SERVICE.</resultMsg></header>
              <body>
                <items>
                  <item>
                    <distance>3.47</distance>
                    <dutyName>연세대학교의과대학강남세브란스병원</dutyName>
                    <latitude>37.492806984645476</latitude>
                    <longitude>127.04631254186797</longitude>
                    <hpid>A1100015</hpid>
                  </item>
                  <item>
                    <distance>3.51</distance>
                    <dutyName>경찰병원</dutyName>
                    <latitude>37.496413213560785</latitude>
                    <longitude>127.12348793503201</longitude>
                    <hpid>A1100039</hpid>
                  </item>
                </items>
                <numOfRows>2</numOfRows>
              </body>
            </response>
            """;

    @Test
    void XML_응답을_매핑한다() {
        // XmlMapper: XML을 자바 객체로 바꿔주는 Jackson의 XML 전용 매퍼.
        // 실제 Client도 이와 같은 방식으로 응답을 파싱할 예정이다.
        EgytLcinfoResponse response = new XmlMapper().readValue(XML, EgytLcinfoResponse.class);

        // header 안의 값이 한 단계 아래 타입(Header)으로 잘 들어갔는지 — 중첩 구조 매핑 확인
        assertEquals("00", response.header().resultCode());

        // items 껍데기 안의 item 2개가 List로 모였는지 — @JacksonXmlElementWrapper 동작 확인
        assertEquals(2, response.body().items().size());

        // 순서가 XML 그대로 유지되는지 (공공데이터가 거리순으로 데이터를 제공하므로 순서 자체가 의미를 가지며 이를 보존해야 한다.)
        assertEquals("경찰병원", response.body().items().get(1).dutyName());

        // 문자열로 온 XML 값이 double로 변환됐는지 — XML은 타입이 없어 전부 문자열로 오므로, Jackson이 타입 변환을 잘 해주는지 확인
        assertEquals(3.47, response.body().items().get(0).distance());
    }
}