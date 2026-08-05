package io.github.soldesk7.gyeot.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import io.github.soldesk7.gyeot.client.GeminiClient;
import io.github.soldesk7.gyeot.dto.RecognitionCategory;
import io.github.soldesk7.gyeot.dto.RecognitionResponse;
import io.github.soldesk7.gyeot.exception.RecognitionRateLimitedException;

@Service
public class RecognitionService {

    private static final Logger log = LoggerFactory.getLogger(RecognitionService.class);

    /**
     * 이 값 미만이면 인식 결과를 그대로 보여주지 않고 수동 선택으로 넘긴다.
     */
    private static final double LOW_CONFIDENCE_THRESHOLD = 0.6;

    private final GeminiClient geminiClient;

    public RecognitionService(GeminiClient geminiClient) {
        this.geminiClient = geminiClient;
    }

    /**
     * 사진을 GeminiClient에 보내 인식 결과를 받는다.
     * 요청 한도 초과는 흡수하지 않고 RecognitionRateLimitedException으로 올려보낸다.
     */
    public RecognitionResponse recognize(byte[] imageBytes, String mimeType) {
        try {
            GeminiClient.Result result = geminiClient.recognize(imageBytes, mimeType);
            RecognitionCategory category = RecognitionCategory.fromJson(result.category());
            return new RecognitionResponse(category, result.confidence(), result.visibleSigns(),
                    isLowConfidence(category, result.confidence(), result.visibleSigns()));
        } catch (RecognitionRateLimitedException e) {
            throw e;    // 계약 응답으로 내보낸다
        } catch (Exception e) {
            // 요청 한도 초과는 앞에서 걸러 계약 응답으로 내보낸다.
            // 나머지는 원인 불문(차단·파싱·네트워크) UNKNOWN으로 흡수 — 죽은 화면 금지(N-01)
            // 사진 바이트는 로그에 남기지 않음(N-03 무로깅) — 예외 정보만 기록
            log.warn("Gemini 인식 실패, unknown으로 폴백", e);
            return new RecognitionResponse(RecognitionCategory.UNKNOWN, 0.0, "", true);
        }
    }

    /**
     * 인식 결과를 확정된 사실처럼 보여줘도 되는지 판정한다. 범주를 못 고른 경우(UNKNOWN)는 확신도와 무관하게 저확신으로 본다 —
     * 보여줄 범주 자체가 없기 때문이다.
     */
    private static boolean isLowConfidence(RecognitionCategory category, double confidence, String visibleSigns) {
        return category == RecognitionCategory.UNKNOWN
                || confidence < LOW_CONFIDENCE_THRESHOLD
                || visibleSigns == null || visibleSigns.isBlank();
    }
}
