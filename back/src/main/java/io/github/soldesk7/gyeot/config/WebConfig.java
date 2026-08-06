package io.github.soldesk7.gyeot.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * 응답을 항상 JSON으로 전송하도록 고정한다.
     *
     * 스프링은 응답을 쓰기 직전에 클라이언트가 보낸 Accept 헤더와 서버가 보낼 수 있는 형식을 맞춰 보고 하나를 고른다. 
     * 공공데이터 XML을 파싱하기 위해 jackson-dataformat-xml을 의존성에 두고 있다.
     * 그것만으로 XML 응답 변환기가 자동 등록되어 후보에 들어간다.
     * 브라우저는 Accept에 application/xml을 명시하면서 아무 형식이나 받겠다는 와일드카드보다 높은 순위를
     * 주므로 같은 엔드포인트가 XML을 돌려준다.
     *
     * 계약(아키텍처 설계 4절) 상 프론트엔드에 전송하는 데이터 형식은 JSON이고 이 서버가 다른 형식을 내보낼 일이 없다.
     */
    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        configurer.ignoreAcceptHeader(true).defaultContentType(MediaType.APPLICATION_JSON);
    }
}
