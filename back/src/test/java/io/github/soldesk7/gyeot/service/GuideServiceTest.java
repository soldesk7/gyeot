package io.github.soldesk7.gyeot.service;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import tools.jackson.databind.ObjectMapper;

import io.github.soldesk7.gyeot.dto.Guide;
import io.github.soldesk7.gyeot.exception.GuideNotFoundException;

/**
 * GuideService의 콘텐츠 로딩과 조회 갈림을 검증한다.
 *
 * 실제 클래스패스의 JSON을 읽는다. 콘텐츠 파일과 record 구조가 어긋나면 필드가 조용히 null이 되고
 * 화면에는 빈 자리로만 보이므로 목을 끼우지 않고 진짜 파일을 파싱해 확인한다.
 */
class GuideServiceTest {

    private final GuideService guideService = new GuideService(new ObjectMapper());

    @Test
    void 목록은_정해진_순서로_세_카테고리를_반환한다() {
        // 화면의 버튼 순서가 서버 재기동마다 바뀌면 안 되므로 순서까지 확인한다.
        List<String> categories = guideService.findAll().stream()
                .map(Guide.Summary::category)
                .toList();

        assertEquals(List.of("burn", "bleeding", "unconscious"), categories);
    }

    @Test
    void 목록은_카테고리와_제목을_담는다() {
        // 카테고리를 고르는 화면이 구획 전체를 받을 이유가 없다.
        Guide.Summary first = guideService.findAll().get(0);

        assertEquals("burn", first.category());
        assertEquals("화상", first.title());
    }

    @Test
    void 정의되지_않은_카테고리는_예외를_던진다() {
        assertThrows(GuideNotFoundException.class, () -> guideService.findByCategory("hangnail"));
    }

    @Test
    void 모든_구획이_제목과_단계와_출처를_갖춘다() {
        // 파일과 record 구조가 어긋나면 예외 없이 필드만 null이 된다.
        // 어느 구획에서 어긋났는지 알 수 없으므로 전부 확인한다.
        Guide guide = guideService.findByCategory("burn");

        assertFalse(guide.sections().isEmpty());
        for (Guide.Section section : guide.sections()) {
            assertNotNull(section.title());
            assertFalse(section.steps().isEmpty());
            assertNotNull(section.source().name());
            assertNotNull(section.source().url());
            assertNotNull(section.source().license());
        }
    }

    @Test
    void 자료가_없는_구획도_media가_빈_배열이_된다() {
        // 파일에 media 키가 아예 없는 구획이 있다. null로 두면 프론트가 매 구획마다 검사해야 한다.
        Guide guide = guideService.findByCategory("burn");

        assertTrue(guide.sections().stream().allMatch(section -> section.media() != null));
    }
}