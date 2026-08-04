package io.github.soldesk7.gyeot.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import tools.jackson.databind.ObjectMapper;

import java.util.Objects;

import io.github.soldesk7.gyeot.dto.Guide;
import io.github.soldesk7.gyeot.exception.GuideNotFoundException;
import io.github.soldesk7.gyeot.exception.GuideUnavailableException;

import tools.jackson.core.JacksonException;
@Service
public class GuideService {

    private static final Logger log = LoggerFactory.getLogger(GuideService.class);

    /**
     * 콘텐츠가 준비된 카테고리. 목록 응답의 순서도 이 순서를 따른다.
     */
    private static final List<String> CATEGORIES = List.of("burn", "bleeding", "unconscious");

    private static final String PATH = "guides/%s.json";

    /**
     * 기동 시 한 번 읽어둔 카테고리별 콘텐츠. 읽기에 실패한 카테고리는 담기지 않는다.
     */
    private final Map<String, Guide> guides;

    public GuideService(ObjectMapper objectMapper) {
        this.guides = GuideService.load(objectMapper);
    }
    
    /**
     * 클래스패스에서 카테고리별 JSON을 읽어 불변 맵으로 만든다.
     *
     * 읽기에 실패한 카테고리는 로그만 남기고 건너뛴다. 예외를 밖으로 던지면 파일 하나가 깨졌을 때
     * 기동 자체가 막히고, 그러면 수동 선택 경로(F-04)가 통째로 사라진다.
     */
    private static Map<String, Guide> load(ObjectMapper objectMapper) {
        Map<String, Guide> loadedGuides = new HashMap<>();
        for (String category : CATEGORIES) {
            Resource resource = new ClassPathResource(PATH.formatted(category));
            try (InputStream in = resource.getInputStream()) {
                Guide guide = objectMapper.readValue(in, Guide.class);
                loadedGuides.put(category, guide);
            } catch (IOException | JacksonException e) {
                log.warn("가이드 콘텐츠 로딩 실패 — {}", category, e);
            }
        }
        return Map.copyOf(loadedGuides);
    }

    /**
     * 콘텐츠가 읽힌 카테고리의 목록을 반환한다.
     *
     * 일부만 읽혔으면 읽힌 것만 반환한다 — 한 카테고리에 문제가 있다고 나머지까지 못 보게 할 이유가 없다.
     * 하나도 읽히지 않은 경우에만 실패로 알린다. 빈 목록을 정상 응답으로 반환하면 화면에 고를 것이
     * 아무것도 없는 채로 오류도 출력되지 않는다.
     */
    public List<Guide.Summary> findAll() {
        if (guides.isEmpty()) {
            throw new GuideUnavailableException("전체");
        }
        return CATEGORIES.stream()
                .map(guides::get)
                .filter(Objects::nonNull)
                .map(Guide.Summary::of)
                .toList();
    }

    /**
     * 카테고리별 콘텐츠를 반환한다.
     *
     * 콘텐츠가 준비된 카테고리인지와 실제로 읽혔는지를 따로 본다. 준비되지 않은 카테고리는 프론트엔드의 잘못된 요청이고 
     * 준비됐는데 읽히지 않은 것은 서버 측 문제라 구분해서 알려야 하기 때문이다.
     */
    public Guide findByCategory(String category) {
        if (!CATEGORIES.contains(category)) {
            throw new GuideNotFoundException();
        }
        Guide guide = guides.get(category);
        if (guide == null) {
            throw new GuideUnavailableException(category);
        }
        return guide;
    }
}
