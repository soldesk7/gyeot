package io.github.soldesk7.gyeot.exception;

/**
 * Gemini가 요청 한도 초과로 429를 돌려줬을 때 던지는 예외.
 *
 * 계약의 429 RATE_LIMITED로 매핑된다.
 * 인식 실패의 다른 원인(안전 필터 거부·타임아웃·파싱)과 달리 200 응답으로 흡수하지 않는다
 *  - 사진이 문제가 아니라 서비스가 바쁜 상태라는 것을 화면이 알려줄 수 있어야 하기 때문이다.
 * 분당 한도인지 일일 한도인지는 구분하지 않는다. 
 *  - 둘 다 429를 받고 구분하려면 오류 본문의 할당량 세부 정보를 파싱해야 한다.
 */
public class RecognitionRateLimitedException extends RuntimeException {
    public RecognitionRateLimitedException(Throwable cause) {
        super(cause);
    }
}
