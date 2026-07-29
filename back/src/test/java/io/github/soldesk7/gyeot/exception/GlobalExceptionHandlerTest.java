package io.github.soldesk7.gyeot.exception;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import io.github.soldesk7.gyeot.controller.EmergencyRoomController;
import io.github.soldesk7.gyeot.controller.RecognitionController;
import io.github.soldesk7.gyeot.service.EmergencyRoomService;
import io.github.soldesk7.gyeot.service.RecognitionService;

/**
 * 필수 입력이 빠진 요청에 계약 형식({ error, message })으로 응답하는지 검증한다.
 *
 * 테스트가 필요한 이유: 예외를 어떤 응답으로 바꿀지는 어노테이션 선언으로만 정해져 있어 매핑이 잘못돼도 컴파일 단계에서는 드러나지 않는다.
 * 두 API가 같은 예외 타입을 던지고 서로 다른 코드로 갈라지는 부분도 실제 요청을 넣어봐야 확인된다.
 *
 * 기대하는 코드 문자열은 상수를 참조하지 않고 리터럴로 적는다 — 프로덕션에서 코드 이름을 바꾸면 이 테스트가 깨져야 한다.
 */
@WebMvcTest({ RecognitionController.class, EmergencyRoomController.class })
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    /** 두 컨트롤러가 주입받는 빈. 요청이 컨트롤러 본문에 도달하지 않으므로 동작은 지정하지 않는다. */
    @MockitoBean
    private RecognitionService recognitionService;

    @MockitoBean
    private EmergencyRoomService emergencyRoomService;

    @Test
    void 사진_없이_인식을_요청하면_MISSING_PHOTO를_반환한다() throws Exception {
        mockMvc.perform(multipart("/api/v1/recognitions"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("MISSING_PHOTO"))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void 좌표_없이_응급실을_조회하면_MISSING_LOCATION을_반환한다() throws Exception {
        mockMvc.perform(get("/api/v1/emergency-rooms"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("MISSING_LOCATION"))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }
}