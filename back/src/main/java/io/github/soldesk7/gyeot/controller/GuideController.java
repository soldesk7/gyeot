package io.github.soldesk7.gyeot.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.github.soldesk7.gyeot.dto.Guide;
import io.github.soldesk7.gyeot.service.GuideService;

@RestController
@RequestMapping("/api/v1/guides")
public class GuideController {

    private final GuideService guideService;

    public GuideController(GuideService guideService) {
        this.guideService = guideService;
    }

    /**
     * 선택 가능한 카테고리 목록을 반환. 수동 선택 화면이 버튼을 출력할 때 사용한다.
     */
    @GetMapping
    public List<Guide.Summary> findAll() {
        return guideService.findAll();
    }

    /**
     * 카테고리 하나의 전체 콘텐츠를 반환.
     *
     * 정의되지 않은 카테고리는 서비스가 GuideNotFoundException을 던지고
     * GlobalExceptionHandler가 404 NOT_FOUND 계약 응답으로 교체한다.
     */
    @GetMapping("/{category}")
    public Guide findByCategory(@PathVariable("category") String category) {
        return guideService.findByCategory(category);
    }

}
