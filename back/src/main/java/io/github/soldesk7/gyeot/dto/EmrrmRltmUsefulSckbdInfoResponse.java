package io.github.soldesk7.gyeot.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;

/**
 * 공공데이터 getEmrrmRltmUsefulSckbdInfoInqire(응급실 실시간 가용병상정보 조회) XML 응답 전용 매핑 DTO.
 *
 * 목록 조회(getEgytListInfoInqire)와 달리 좌표로는 부를 수 없고 주소(시도·시군구)가 필수다.
 * 그래서 기관 목록의 dutyAddr에서 시도·시군구를 잘라 이 오퍼레이션을 호출하고 돌아온 결과를 hpid로 목록과 짝지어 가용병상을 채운다.
 *
 * 응답 구조는 목록 조회와 같다(response → header/body → items → item).
 *
 * 응답에는 60개가 넘는 필드가 온다 — 병상 종류별 수치(hvs01~hvs59), 장비 가용 여부(hvctayn 등), 당직의 연락처까지 포함된다.
 * 우리에게 필요한 3개만 선언하고 나머지는 {@code @JsonIgnoreProperties(ignoreUnknown = true)}로 버린다. 
 * 이 설정이 없으면 선언하지 않은 필드를 만나는 순간 예외가 발생하며 파싱이 통째로 실패한다.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record EmrrmRltmUsefulSckbdInfoResponse(Header header, Body body) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Header(String resultCode, String resultMsg) {

    }

    /**
     * 이 타입만 record가 아닌 이유는 EgytListInfoResponse.Body와 동일 - 
     * @JacksonXmlElementWrapper가 record에서 동작하지 않는 Jackson 버그(jackson-dataformat-xml#517).
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class Body {

        @JacksonXmlElementWrapper(localName = "items")  // 껍데기 엘리먼트 이름
        @JacksonXmlProperty(localName = "item")         // 반복되는 항목 이름
        private List<Item> items;

        /** 조회 결과가 0건이면 null이 될 수 있다 — 호출부에서 방어. */
        public List<Item> items() {
            return items;
        }
    }

    /**
     * 응급실 1곳의 실시간 병상 정보.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Item(
            String hpid,     // 기관ID -> 기관 목록의 Item.hpid와 짝지어 매칭한다.
            Integer hvec,    // 응급실 일반 가용병상 -> EmergencyRoom.availableBeds
                             // 0은 만실, 음수는 정원 초과 인원을 뜻한다(실측: -1·-14·-25).
                             // 결측 가능성에 대비해 래퍼 타입으로 받는다.
            String hvidate   // 입력일시. 14자리 문자열(20260730161620), 시간대 표기가 없는 한국 시각.
                             // 기관마다 갱신 시점이 달라 응급실 목록의 asOf와는 별개의 값이다.
            ) {

    }
}