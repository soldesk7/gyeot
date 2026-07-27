package io.github.soldesk7.gyeot.dto;

public record EmergencyRoom(
    String name,           // 기관명 ← dutyName
    double lat,            // 위도 ← latitude
    double lng,            // 경도 ← longitude
    Integer availableBeds, // 가용병상 — SP2에서는 null (병상 API 조합은 SP3)
    int distanceM          // 거리(미터) ← distance(km) × 1000
) {
    
}
