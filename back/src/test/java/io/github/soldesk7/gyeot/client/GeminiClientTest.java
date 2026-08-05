package io.github.soldesk7.gyeot.client;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.google.genai.errors.ClientException;
import com.google.genai.errors.GenAiIOException;
import com.google.genai.errors.ServerException;

/**
 * GeminiClient가 요청 한도 초과를 알아보는지 검증한다.
 *
 * SDK 예외는 그대로 올라오지 않는다. spring-ai가 RuntimeException("Failed to generate content")으로
 * 감싸 던지므로 겉면만 보면 다른 실패와 구분되지 않는다. 판별이 어긋나면 한도 초과가 조용히
 * unknown 폴백으로 흡수되어 화면이 "인식하지 못했어요"만 보여준다 — 오류가 나지 않아 눈치채기 어렵다.
 *
 * 예외를 직접 만들어 확인하므로 Gemini를 부르지 않는다.
 */
class GeminiClientTest {

    /** spring-ai가 SDK 예외를 감쌀 때 쓰는 문구. */
    private static final String WRAPPER = "Failed to generate content";

    @Test
    void 감싸인_429를_찾아낸다() {
        // 실제로 올라오는 형태 — SDK 예외가 원인으로 붙는다.
        assertTrue(GeminiClient.isRateLimited(
                new RuntimeException(WRAPPER, new ClientException(429, "RESOURCE_EXHAUSTED", "quota exceeded"))));

        // 감싸지 않고 그대로 올라오는 경우도 같은 결과여야 한다.
        assertTrue(GeminiClient.isRateLimited(
                new ClientException(429, "RESOURCE_EXHAUSTED", "quota exceeded")));
    }

    @Test
    void 한도_초과가_아니면_거짓이다() {
        // 같은 ApiException이지만 다른 상태 코드 - 타입만 보고 판단하면 안 된다.
        assertFalse(GeminiClient.isRateLimited(
                new RuntimeException(WRAPPER, new ClientException(400, "INVALID_ARGUMENT", "bad request"))));
        assertFalse(GeminiClient.isRateLimited(
                new RuntimeException(WRAPPER, new ServerException(503, "UNAVAILABLE", "overloaded"))));

        // 시연 중 실제로 났던 타임아웃.
        assertFalse(GeminiClient.isRateLimited(
                new RuntimeException(WRAPPER, new GenAiIOException("Failed to execute HTTP request."))));

        // 원인이 없어도 사슬 순회가 끝나야 한다.
        assertFalse(GeminiClient.isRateLimited(new RuntimeException(WRAPPER)));
    }
}