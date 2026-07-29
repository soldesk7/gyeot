package io.github.soldesk7.gyeot.dto;

import java.time.Instant;
import java.util.List;

public record EmergencyRoomsResponse(
        Instant asOf,                // 데이터 기준 시각 (SP2: 조회 시각, SP3: 캐시 기준 시각)
        List<EmergencyRoom> items
) {
}