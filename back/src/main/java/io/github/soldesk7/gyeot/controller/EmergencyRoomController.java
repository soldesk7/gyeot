package io.github.soldesk7.gyeot.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.github.soldesk7.gyeot.dto.EmergencyRoomsResponse;
import io.github.soldesk7.gyeot.service.EmergencyRoomService;

/**
 * 응급실 조회 API(F-05).
 *
 * 컨트롤러는 HTTP 세계와 도메인 세계의 접점만 담당한다 — 쿼리 파라미터를 받아 Service에 전달하고 돌려받은 계약 DTO를 그대로
 * 응답한다. 공공데이터를 어떻게 부르는지, 거리 단위가 무엇인지는 여기서 알 필요 없다.
 */
@RestController
@RequestMapping("/api/v1/emergency-rooms")   // 이 컨트롤러의 공통 호출 경로
public class EmergencyRoomController {

    private final EmergencyRoomService emergencyRoomService;

    public EmergencyRoomController(EmergencyRoomService emergencyRoomService) {
        this.emergencyRoomService = emergencyRoomService;
    }

    /**
     * 좌표 기준 인근 응급실 목록을 반환
     *
     * lat·lng는 필수다. 
     * @RequestParam은 기본값이 required = true이므로 누락 시 스프링이 MissingServletRequestParameterException을 던진다.
     * 이를 GlobalExceptionHandler가 400 MISSING_LOCATION 계약 응답으로 교체한다.
     *
     * 좌표는 저장하지 않는다 — 조회 요청에만 사용한다.(N-03).
     */
    @GetMapping
    public EmergencyRoomsResponse findNearby(@RequestParam("lat") double lat,
            @RequestParam("lng") double lng) {
        return emergencyRoomService.findNearby(lat, lng);
    }
}
