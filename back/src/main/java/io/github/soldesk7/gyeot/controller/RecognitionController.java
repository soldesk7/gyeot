package io.github.soldesk7.gyeot.controller;

import java.io.IOException;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import io.github.soldesk7.gyeot.dto.RecognitionCategory;
import io.github.soldesk7.gyeot.dto.RecognitionResponse;
import io.github.soldesk7.gyeot.service.RecognitionService;


@RestController
@RequestMapping("/api/v1/recognitions")   // 이 컨트롤러의 공통 접두 경로
public class RecognitionController {
    private final RecognitionService recognitionService;

    public RecognitionController(RecognitionService recognitionService) {
        this.recognitionService = recognitionService;
    }

    @PostMapping    
    public RecognitionResponse recognize(@RequestParam("photo") MultipartFile photo) {
        try {
            return recognitionService.recognize(photo.getBytes(), photo.getContentType());
        } catch (IOException e) {
            // 파일 읽기 실패도 인식 실패의 일종 — 죽은 화면 금지(N-01), 서비스와 같은 폴백
            return new RecognitionResponse(RecognitionCategory.UNKNOWN, 0.0, "", true);
        }
    }
}