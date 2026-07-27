package io.github.soldesk7.gyeot.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


/**
 * Jackson 애너테이션은 두 곳에서 온다 — 헷갈리기 쉬운 지점이다.
     * com.fasterxml.jackson.annotation : 형식(JSON/XML)과 무관한 공용 애너테이션
     * tools.jackson.dataformat.xml     : XML 전용 애너테이션 (Jackson 3부터 groupId가 tools.jackson)
 * 인터넷 예제 대부분은 Jackson 2 기준이라 XML 쪽도 com.fasterxml...로 되어 있다. 그대로 쓰면 안 된다.
*/
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;
/**
 *
 
 */
/**
 * 공공데이터 getEgytLcinfoInqire(응급의료기관 위치정보 조회) XML 응답 전용 매핑 DTO.
 * 
 * 우리가 XML을 한 줄씩 파싱하는 코드를 쓰는 대신 "이 XML은 이런 모양의 객체다"라고 클래스로 선언해두면 Jackson이 알아서 채워준다. 그 선언을 위한 파일.
 * 
 * 실제 응답 구조:
 * <pre>{@code
 * <response>
 *   <header>
 *     <resultCode>00</resultCode>            <!-- 00이 정상 -->
 *     <resultMsg>NORMAL SERVICE.</resultMsg>
 *   </header>
 *   <body>
 *     <items>                                <!-- 반복 항목을 감싸는 껍데기 -->
 *       <item>...</item>                     <!-- 병원 1곳 (거리순 정렬됨) -->
 *       <item>...</item>
 *     </items>
 *     <numOfRows>20</numOfRows>
 *   </body>
 * </response>
 * }</pre>
 * 
 * XML의 계층 구조가 그대로 자바의 중첩 타입 구조가 된다:
 *   response → EgytLcinfoResponse
 *   header   → Header
 *   body     → Body
 *   item     → Item
 * 
 * 벤더(공공데이터포털) 필드명을 그대로 쓴다 — 매핑 대상이 뭔지 명확해지기 때문이다.
 * 대신 이 타입은 client 경계 밖(Service·Controller)으로 나가지 않는다.
 * 바깥으로는 우리 계약 DTO(EmergencyRoom)로 변환해서 내보낸다.
 * 
 * 모든 타입에 {@code @JsonIgnoreProperties(ignoreUnknown = true)}를 붙인 이유:
 * "선언하지 않은 필드가 XML에 있어도 무시하라"는 뜻
 * 기본값은 정반대. 모르는 필드를 만나면 예외를 던지고 파싱이 통째로 실패한다.
 * 응답에는 14개 필드가 오는데 우리는 이중 4개의 필드만 사용하므로 이 설정이 반드시 필요하다.
*/
@JsonIgnoreProperties(ignoreUnknown = true)
public record EgytLcinfoResponse(Header header, Body body) {

    /** 
     * 응답 처리 결과. resultCode가 "00"이면 정상, 그 외는 오류다. 
     * (필드명 resultCode/resultMsg가 XML 태그명과 같아서 애너테이션 없이 매핑된다)
    */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Header(String resultCode, String resultMsg) {
    }

    /**
     * 응답 본문. 원본 xml이 items 안에 item이 반복되는 구조이므로 List로 받는다.
     *
     * 이 타입만 record가 아닌 이유: @JacksonXmlElementWrapper가 record에서 동작하지 않는 Jackson 버그(jackson-dataformat-xml#517) 때문이다.
     * record로 두면 "Could not find creator property with name 'items'" 오류가 난다.
     * record는 "생성자 파라미터 이름 = 필드 이름"인데 @JacksonXmlProperty가 그 이름을 item으로 바꿔버리므로 items라는 이름을 찾는 wrapper와 어긋나기 때문이다.
     * 일반 클래스는 필드와 생성자가 분리돼 있어 이 충돌이 없다.
     * TODO: 라이브러리에서 버그 픽스 되면 record로 되돌릴 것.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class Body {

        /**
         * Jackson은 "이 필드에 붙은 애너테이션"을 보고 값을 채운다.
            * @JacksonXmlElementWrapper(localName = "items") : 반복 항목을 감싸는 껍데기 태그 이름
            * @JacksonXmlProperty(localName = "item")        : 껍데기 안에서 반복되는 태그 이름
         * 이 둘이 짝을 이뤄야 <items><item/><item/></items> 구조가 List로 모인다.
         * 
         * setter가 없는데도 값이 채워지는 이유: Jackson이 리플렉션으로 private 필드에 직접 값을 넣기 때문이다(접근 제한자를 우회하는 자바 표준 기능).
         * 즉 매핑의 기준은 "메서드"가 아니라 "애너테이션이 붙은 필드"다.
         */
        @JacksonXmlElementWrapper(localName = "items")  // 껍데기 엘리먼트 이름
        @JacksonXmlProperty(localName = "item")         // 반복되는 항목 이름
        private List<Item> items;

        /** 조회 결과가 0건이면 null이 될 수 있다 — 호출부에서 방어. 
         * 이 메서드는 Jackson이 쓰는 게 아니라 우리 코드가 값을 꺼낼 때 쓰는 접근자.
         * 이름을 getItems()가 아니라 items()로 지은 이유: 이 파일의 나머지가 전부 record라 호출부에서 response.body().items()처럼 일관된 모양이 되게 하기 위함.
             * (자바빈 규약인 getXxx()가 아니어서 Jackson이 getter로 오인할 일도 없다)
        */
        public List<Item> items() {
            return items;
        }
    }

    /**
     * 응급의료기관 1곳. 응답에는 주소·전화·기관ID 등도 오지만 우리 계약(EmergencyRoom)에 필요한 4개만 받는다.
     * 선언하지 않은 나머지는 위의 @JsonIgnoreProperties 덕분에 그냥 버려진다.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Item(
            String dutyName,   // 기관명
            double latitude,   // 위도
            double longitude,  // 경도
            double distance    // 거리(km) -> EmergencyRoom.distanceM (×1000 변환은 Service가 담당)
    ) {
    }
}
