package io.github.soldesk7.gyeot.client;

import io.github.soldesk7.gyeot.dto.EgytLcinfoResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import tools.jackson.dataformat.xml.XmlMapper;

import java.time.Duration;
import java.util.List;

/**
 * 공공데이터포털 응급의료정보조회서비스(ErmctInfoInqireService) 호출 담당.
 *
 * 이 클래스가 벤더(공공데이터포털)에 관한 모든 것을 떠안는다 — 인증키, URL, XML 형식, 응답 구조. 그래서
 * Service·Controller는 공공데이터가 XML을 주는지 JSON을 주는지조차 알 필요가 없다. 나중에 데이터 출처가 바뀌어도 이 파일만 수정하면 된다.
 */
@Component
public class EmergencyRoomClient {

    private static final String BASE_URL = "http://apis.data.go.kr/B552657/ErmctInfoInqireService";

    // 응급의료기관 위치정보 조회 — 경/위도 기준으로 가까운 순으로 돌려준다.
    private static final String PATH_NEARBY = "/getEgytLcinfoInqire";

    // 지도에 뿌릴 마커 수 상한. 이 API에는 반경 파라미터가 없어서 "가까운 순 N개"로만 제한할 수 있다.
    private static final int NUM_OF_ROWS = 20;

    private final RestClient restClient;
    private final String serviceKey;

    /** XML을 자바 객체로 바꿔주는 Jackson 매퍼. 
     * 스프링부트 4.1은 XmlMapper 빈을 자동 등록하지 않으므로(JSON용 JsonMapper만 등록) 여기서 직접 만든다. 
     * XML을 아는 코드가 이 클래스 안에만 머무는 효과도 있다.
    */
    private final XmlMapper xmlMapper = new XmlMapper();

    public EmergencyRoomClient(RestClient.Builder builder,
            @Value("${gyeot.public-data.api-key}") String serviceKey) {
        this.serviceKey = serviceKey;

        /** 
         * RestClient는 실제 HTTP 통신을 직접 하지 않고, ClientHttpRequestFactory에게 연결 생성을 맡긴다. 
         * 어떤 구현체를 사용하느냐에 따라 밑단 HTTP 엔진이 바뀐다(JDK 기본, Apache HttpClient, Netty 등). 
         * SimpleClientHttpRequestFactory는 그중 JDK 내장 HttpURLConnection을 사용하는 가장 단순한 구현. 
         * 추가 의존성이 필요 없어 이 정도 호출량에는 충분하다 생각한다.
         * 
         * 외부 API 사용 시 반드시 응답 시간 상한(타임아웃)을 설정한다.
         * 타임아웃이 없으면 공공데이터 쪽이 응답하지 않을 때 화면이 계속 대기 상태에 머문다.
         * 응급 상황에서는 늦게라도 성공하는 것보다, 빨리 실패해서 다른 경로(E-Gen 안내 등)로 넘겨주는 편이 낫다(N-01·N-07).
        */
        var requestFactory = new SimpleClientHttpRequestFactory();

        /** connect: 서버와 연결을 맺기까지 기다리는 시간. 서버가 죽었거나 네트워크가 막힌 경우 여기서 걸린다.*/
        requestFactory.setConnectTimeout(Duration.ofSeconds(3));

        /** 
         * read: 연결된 뒤 응답 데이터를 기다리는 시간. 연결은 됐는데 서버가 처리를 못 끝내는 경우 걸린다.
         * 공공데이터 문서상 이 API의 평균 응답 시간이 500ms이므로, 5초로 10배 여유를 잡았다.
         */
        requestFactory.setReadTimeout(Duration.ofSeconds(5));

        this.restClient = builder
                .baseUrl(BASE_URL)
                .requestFactory(requestFactory)
                .build();
    }

    /**
     * 좌표 기준으로 가까운 응급의료기관을 조회한다. 정렬은 공공데이터가 이미 거리순으로 해주므로 재정렬 불필요.
     */
    public List<EgytLcinfoResponse.Item> findNearby(double lat, double lng) {
        // 응답을 String으로 받아 직접 파싱한다. RestClient가 XML을 자동 변환해주길 기대할 수도
        // 있지만, 스프링부트 4.1에는 XML 메시지 컨버터가 자동 등록되지 않는다.
        String xml = restClient.get()
                .uri(uriBuilder -> uriBuilder
                .path(PATH_NEARBY)
                .queryParam("serviceKey", serviceKey)
                .queryParam("WGS84_LAT", lat)
                .queryParam("WGS84_LON", lng)
                .queryParam("pageNo", 1)
                .queryParam("numOfRows", NUM_OF_ROWS)
                .build())
                .retrieve()
                .body(String.class);

        EgytLcinfoResponse response = xmlMapper.readValue(xml, EgytLcinfoResponse.class);

        // 조회 결과가 0건이면 XML에 <item>이 없어 items가 null이 된다. 여기서 막지 않으면
        // 호출부에서 NPE가 난다.
        List<EgytLcinfoResponse.Item> items = response.body().items();
        return items == null ? List.of() : items;
    }
}
