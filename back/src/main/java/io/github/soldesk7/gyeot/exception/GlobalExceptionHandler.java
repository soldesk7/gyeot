package io.github.soldesk7.gyeot.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import io.github.soldesk7.gyeot.dto.ErrorResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 두 핸들러에서 같은 응답을 반환하므로 문구를 한 곳에서 관리한다.
     * record는 불변이라 공유해도 안전하다.
     */
    private static final ErrorResponse MISSING_PHOTO = new ErrorResponse("MISSING_PHOTO", "사진을 다시 선택해 주세요.");
    private static final ErrorResponse MISSING_LOCATION = new ErrorResponse("MISSING_LOCATION", "위치 정보를 가져오지 못했어요.");

    /**
     * 필수 쿼리 파라미터 누락. 두 API가 같은 예외를 던지므로 파라미터명으로 계약 코드를 가른다. message는 사용자에게 그대로
     * 보이는 문구다 — 예외 원문(내부 시그니처 포함)을 싣지 않는다.
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMissingParameter(MissingServletRequestParameterException ex) {
        return switch (ex.getParameterName()) {
            case "lat", "lng" -> MISSING_LOCATION;
            default -> MISSING_PHOTO;
        };
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.CONTENT_TOO_LARGE)
    public ErrorResponse handlePhotoTooLarge() {
        return new ErrorResponse("PHOTO_TOO_LARGE", "너무 큰 사진을 업로드했습니다. 작은 사진을 선택해 주세요.");
    }

    /**
     * photo 파트 누락. multipart 요청인데 파트가 없으면 MissingServletRequestPartException.
     * 애초에 multipart 요청이 아니면 MultipartException이 올라온다.
     * MaxUploadSizeExceededException은 MultipartException의 하위지만 위 전용 핸들러가 더
     * 구체적이라 그쪽이 이긴다.
     */
    @ExceptionHandler({MissingServletRequestPartException.class, MultipartException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMissingPhoto() {
        return MISSING_PHOTO;
    }

    /**
     * lat·lng 형변환 실패(빈 값·"undefined" 등). 누락과 프론트 동작이 같아 계약 코드를 재사용한다.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleInvalidLocation() {
        return MISSING_LOCATION;
    }

    /**
     * 공공데이터 조회 실패. 원인(타임아웃·5xx·파싱)은 cause에만 있으므로 반드시 로그에 남긴다.
     */
    @ExceptionHandler(HospitalDataUnavailableException.class)
    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    public ErrorResponse handleHospitalDataUnavailable(HospitalDataUnavailableException ex) {
        log.warn("공공데이터 응급실 조회 실패", ex);
        return new ErrorResponse("HOSPITAL_DATA_UNAVAILABLE", "응급실 정보를 불러오지 못했어요.");
    }

    /**
     * 위 핸들러들이 잡지 않은 나머지 모든 예외를 처리. 여기 오는 예외는 두 종류가 섞여 있다.
     *
     * 하나는 스프링이 상태코드를 이미 정해 둔 예외들 — 없는 경로(404)·허용되지 않은 HTTP 메서드(405) 등이
     * org.springframework.web.ErrorResponse를 구현하고 있어 자기 상태코드를 들고 온다. 이 경우 그 코드를
     * 그대로 사용한다. 다른 하나는 우리가 예상하지 못한 예외이고 이때만 500으로 응답한다.
     *
     * 어느 쪽이든 응답 바디는 계약 형식을 유지한다(아키텍처 4절). 로그는 5xx일 때만 남긴다. 4xx는 클라이언트가 잘못 부른
     * 것이라 서버 로그로 출력할 이유가 없다.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleInternalError(Exception ex) {
        HttpStatusCode status = (ex instanceof org.springframework.web.ErrorResponse er)
                ? er.getStatusCode()
                : HttpStatus.INTERNAL_SERVER_ERROR;

        if (status.is5xxServerError()) {
            log.warn("처리되지 않은 예외", ex);
        }
        return ResponseEntity.status(status)
                .body(new ErrorResponse("INTERNAL_ERROR", "서버에 문제가 발생했습니다. 잠시 후 다시 시도해 주세요."));
    }
}
