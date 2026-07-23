package io.github.soldesk7.gyeot.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import io.github.soldesk7.gyeot.client.GeminiClient;
import io.github.soldesk7.gyeot.dto.RecognitionCategory;
import io.github.soldesk7.gyeot.dto.RecognitionResponse;

@Service
public class RecognitionService {
    private static final Logger log = LoggerFactory.getLogger(RecognitionService.class);

    private final GeminiClient geminiClient;

    public RecognitionService(GeminiClient geminiClient) {
        this.geminiClient = geminiClient;
    }

    public RecognitionResponse recognize(byte[] imageBytes, String mimeType) {
        try {
            GeminiClient.Result result = geminiClient.recognize(imageBytes, mimeType);
            RecognitionCategory category = RecognitionCategory.fromJson(result.category());
            return new RecognitionResponse(category, result.confidence(), result.visibleSigns());
        } catch (Exception e) {
            // 원인 불문(차단·파싱·네트워크) UNKNOWN으로 흡수 — 죽은 화면 금지(N-01), 원인별 세분화는 SP3
            // 사진 바이트는 로그에 남기지 않음(N-03 무로깅) — 예외 정보만 기록
            log.warn("Gemini 인식 실패, unknown으로 폴백", e);
            return new RecognitionResponse(RecognitionCategory.UNKNOWN, 0.0, "");
        }
    }
}