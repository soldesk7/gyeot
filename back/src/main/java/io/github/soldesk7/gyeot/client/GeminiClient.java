package io.github.soldesk7.gyeot.client;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.google.genai.GoogleGenAiChatOptions;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeTypeUtils;

@Component
public class GeminiClient {

    private final ChatClient chatClient;

    private static final String PROMPT = """
            당신은 사진에서 '보이는 것'만 관찰해 보고하는 도구다. 진단하거나 처치를 지시하지 마라.
            아래 JSON 형식으로만 답하라:
            {"category": "burn|bleeding|unconscious|unknown", "confidence": 0.0~1.0 숫자, "visibleSigns": "관찰된 시각적 특징 1문장"}
            규칙: burn=화상 흔적, bleeding=출혈/외상 흔적, unconscious=의식 없이 쓰러진 사람으로 보임.
            해당 없음/판단 불가/부상 아님이면 반드시 "unknown". 치료법·조언·진단명은 절대 출력하지 마라.
            """;

    public GeminiClient(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    public Result recognize(byte[] imageBytes, String mimeType) {
        return chatClient.prompt() // 1. 요청 빌더 시작
                .user(u -> u // 2. "user" 역할 메시지 조립
                        .text(PROMPT) // 텍스트: 우리가 정한 고정 프롬프트
                .media(MimeTypeUtils.parseMimeType(mimeType), new ByteArrayResource(imageBytes))) // 미디어: 업로드된 사진 (멀티모달 입력)
                .options(GoogleGenAiChatOptions.builder() // 3. 이번 요청에만 적용할 옵션
                        .thinkingBudget(0) // thinking 끄기 — 안 끄면 JSON이 중간에 잘림
                        .maxOutputTokens(4000)) // 최대 토큰 수 제한 (여유 있게)
                .call() // 4. 실제 Gemini api에 요청 전송
                .entity(Result.class); // 5. 응답 텍스트를 Result record로 자동 파싱
    }

    public record Result(
        String category,       // burn|bleeding|unconscious|unknown 중 하나 — §4 API 계약의 category 값과 동일
        double confidence,     // 0.0~1.0, 모델이 스스로 매긴 확신도 (0.6 미만 처리는 SP3에서 결정)
        String visibleSigns    // 판단 근거가 된 시각적 특징 1문장 — 진단·처치 문구 아님, 관찰 서술만
    ) {}
}
