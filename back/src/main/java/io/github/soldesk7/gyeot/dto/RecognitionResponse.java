package io.github.soldesk7.gyeot.dto;

public record RecognitionResponse(RecognitionCategory category, double confidence, String visibleSigns, boolean lowConfidence) {

}
