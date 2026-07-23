package io.github.soldesk7.gyeot.dto;

import com.fasterxml.jackson.annotation.JsonValue;

public enum RecognitionCategory {
    BURN, // 화상
    BLEEDING, // 외상•출혈
    UNCONSCIOUS, // 의식불명
    UNKNOWN; // 알 수 없음

    /*
    * JSON 직렬화/역직렬화 시 enum 이름을 소문자로 변환해주는 메서드
    * Jackson이 enum을 JSON으로 내보낼 때 기본값(대문자 "BURN")이 아니라
    * API 계약의 소문자 값("burn")으로 나가게 함 — @JsonValue가 직렬화 시 이 메서드를 쓰도록 지정
    */
    @JsonValue
    public String toJson() {
        return name().toLowerCase();
    }

    /*
    * Gemini가 돌려준 JSON에서 enum으로 역직렬화하는 메서드
    * API 요청에서 받은 소문자 값("burn")을 대문자 enum 값으로 변환
    * 예상 못한 값이 와도 예외가 나오지 않고 UNKNOWN으로 안전하게 폴백 (N-01, 죽은 화면 금지)
    */
    public static RecognitionCategory fromJson(String value) {
        try {
            return RecognitionCategory.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            return UNKNOWN;
        }
    }
}
