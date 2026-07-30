package io.github.soldesk7.gyeot.dto;

public record ErrorResponse (
    String error,
    String message
) {
}