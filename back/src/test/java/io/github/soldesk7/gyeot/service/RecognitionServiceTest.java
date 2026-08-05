package io.github.soldesk7.gyeot.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.github.soldesk7.gyeot.client.GeminiClient;
import io.github.soldesk7.gyeot.dto.RecognitionCategory;
import io.github.soldesk7.gyeot.dto.RecognitionResponse;
import io.github.soldesk7.gyeot.exception.RecognitionRateLimitedException;

/**
 * RecognitionService의 저확신 판정 규칙을 검증한다.
 *
 * 이 판정은 응답의 boolean 하나로만 드러나고 값이 뒤집혀도 화면은 정상으로 보인다. 다만 참이어야 할 때 거짓이면 확신 없는 인식
 * 결과가 확정된 사실처럼 노출되어 비진단 원칙을 어기고 반대로 거짓이어야 할 때 참이면 멀쩡한 인식 결과가 매번 수동 선택으로 밀려난다.
 * 그래서 기준값 경계와 범주 미선택을 값으로 고정해 둔다.
 * 요청 한도 초과만 예외로 올려보내고 나머지 실패는 흡수하는지도 함께 확인한다.
 *
 * Client를 목으로 대체하므로 Gemini를 부르지 않는다.
 */
public class RecognitionServiceTest {

    /**
     * 인식 대상 사진. 목이 응답을 정해두므로 내용은 결과에 영향을 주지 않는다.
     */
    private static final byte[] PHOTO = {1, 2, 3};

    private static final String MIME = "image/jpeg";

    @Test
    void 확신도가_기준값_미만이면_저확신이다() {
        assertTrue(인식("burn", 0.59).lowConfidence());
    }

    @Test
    void 확신도가_기준값과_같으면_저확신이_아니다() {
        // 기준값 미만만 저확신이다. 경계에서 판정이 뒤집히는 지점을 고정한다.
        RecognitionResponse response = 인식("burn", 0.6);

        assertFalse(response.lowConfidence());
        assertEquals(RecognitionCategory.BURN, response.category());

    }

    @Test
    void 범주를_고르지_못하면_확신도가_높아도_저확신이다() {
        // "부상이 아니다"라고 확신하는 경우에도 unknown이 온다.
        // 확신도는 높지만 보여줄 범주가 없으므로 수동 선택으로 넘겨야 한다.
        assertTrue(인식("unknown", 0.95).lowConfidence());
    }

    @Test
    void 인식에_실패하면_저확신으로_응답한다() {
        GeminiClient client = mock(GeminiClient.class);
        when(client.recognize(PHOTO, MIME)).thenThrow(new RuntimeException("안전 필터 차단"));
        RecognitionService service = new RecognitionService(client);

        RecognitionResponse response = service.recognize(PHOTO, MIME);

        // 예외를 밖으로 던지지 않고 응답을 만들어 돌려준다 — 죽은 화면 금지(N-01).
        assertEquals(RecognitionCategory.UNKNOWN, response.category());
        assertEquals(0.0, response.confidence());
        assertEquals("", response.visibleSigns());
        assertTrue(response.lowConfidence());
    }

    @Test
    void 관찰_문장이_없으면_확신도가_높아도_저확신이다() {
        // 응답에 필드가 빠지면 null, 빈 문자열로 오면 공백이 된다.
        // 어느 쪽이든 확인 화면에 띄울 내용이 없으므로 수동 선택으로 넘긴다.
        assertTrue(인식("burn", 0.9, null).lowConfidence());
        assertTrue(인식("burn", 0.9, "  ").lowConfidence());
    }

    /**
     * Gemini가 주어진 범주·확신도로 답했을 때의 인식 결과.
     */
    private static RecognitionResponse 인식(String category, double confidence) {
        return 인식(category, confidence, "관찰된 시각적 특징");
    }
    @Test
    void 요청_한도_초과는_흡수하지_않고_올려보낸다() {
        // 다른 실패와 달리 화면이 다르게 안내해야 하므로 계약 응답으로 나가야 한다.
        GeminiClient client = mock(GeminiClient.class);
        when(client.recognize(PHOTO, MIME))
                .thenThrow(new RecognitionRateLimitedException(new RuntimeException("429")));
        RecognitionService service = new RecognitionService(client);

        assertThrows(RecognitionRateLimitedException.class, () -> service.recognize(PHOTO, MIME));
    }

    /**
     * Gemini가 관찰 문장까지 지정해 답했을 때의 인식 결과.
     */
    private static RecognitionResponse 인식(String category, double confidence, String visibleSigns) {
        GeminiClient client = mock(GeminiClient.class);
        when(client.recognize(PHOTO, MIME))
                .thenReturn(new GeminiClient.Result(category, confidence, visibleSigns));
        return new RecognitionService(client).recognize(PHOTO, MIME);
    }
}
