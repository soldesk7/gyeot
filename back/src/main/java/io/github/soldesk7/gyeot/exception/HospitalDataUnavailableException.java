package io.github.soldesk7.gyeot.exception;

/**
 * 공공데이터포털 응급실 조회가 실패했을 때 던지는 예외(타임아웃·5xx·XML 파싱 실패).
 *
 * 벤더 예외(RestClientException·JacksonException)를 EmergencyRoomClient 밖으로 내보내지 않기 위한 경계.
 * 이 예외 하나만 502 HOSPITAL_DATA_UNAVAILABLE로 매핑하면 되므로, 데이터 출처가 바뀌어도 핸들러를 변경하지 않아도 된다.
 */
public class HospitalDataUnavailableException extends RuntimeException {

    public HospitalDataUnavailableException(Throwable cause) {
        super(cause);
    }
}