package io.github.soldesk7.gyeot.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.genai.Client;
import com.google.genai.types.HttpOptions;

@Configuration
public class GeminiClientConfig {

    /**
     * Gemini 호출에 사용하는 SDK 클라이언트.
     *
     * SDK는 타임아웃을 지정하지 않으면 연결·읽기·쓰기 제한을 모두 0(무제한)으로 둔다. 
     * 여기서 지정한 값은 OkHttp의 callTimeout으로 적용되어 연결·전송·SDK 내부 재시도까지 한 호출 전체에 적용된다.
     *
     * spring-ai 자동설정에는 타임아웃 설정 항목이 없고 자동설정의 Client 빈이 @ConditionalOnMissingBean(커스텀 빈 등록 시 기본 자동설정 빈 생성을 스킵하게 만드는 설정 빈)이므로 이 빈이 등록되면 자동설정 대신 사용된다. 
     * 모델·토큰 같은 나머지 설정은 자동설정이 그대로 담당한다.
     */
    @Bean // 이 메서드가 반환하는 Client 객체를 Spring Bean으로 등록
    public Client googleGenAiClient(
            @Value("${spring.ai.google.genai.api-key}") String apiKey,
            @Value("${gyeot.gemini.timeout-ms}") int timeoutMs
        ) {
        return Client.builder()
                .apiKey(apiKey)
                .httpOptions(HttpOptions.builder().timeout(timeoutMs).build())
                .build();
    }
}
