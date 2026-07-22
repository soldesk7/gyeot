package io.github.soldesk7.gyeot.dto;

import com.fasterxml.jackson.annotation.JsonValue;

public enum RecognitionCategory {
    BURN, // 화상
    BLEEDING, // 외상•출혈
    UNCONSCIOUS, // 의식불명
    UNKNOWN; // 알 수 없음

    @JsonValue
    public String toJson() {
        return name().toLowerCase();
    }
}
