package io.github.soldesk7.gyeot.exception;

/**
 * 정의되지 않은 카테고리로 가이드를 요청했을 때 던지는 예외.
 *
 * 404 NOT_FOUND로 매핑된다. 콘텐츠가 준비된 세 카테고리(화상·출혈외상·의식저하) 밖의 값이 경로 변수로 들어온 경우이며 서버
 * 문제가 아니라 요청이 잘못된 경우 발생하는 예외다.
 */
public class GuideNotFoundException extends RuntimeException {

    public GuideNotFoundException() {
    }
}
