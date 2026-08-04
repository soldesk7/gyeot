package io.github.soldesk7.gyeot.exception;

/**
 * 알려진 카테고리인데 콘텐츠 파일을 읽지 못해 응답할 수 없을 때 던지는 예외.
 *
 * 503 GUIDE_UNAVAILABLE로 매핑된다. 요청은 올바르나 서버가 자료를 갖고 있지 않은
 * 상태이며 어느 카테고리인지는 메시지에 담아 로그에서 구분한다.
 */
public class GuideUnavailableException extends RuntimeException {

    public GuideUnavailableException(String category) {
        super(category);
    }
}