package io.github.soldesk7.gyeot.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 예약 작업 활성화.
 *
 * 스프링부트는 @Scheduled가 붙은 메서드를 그냥 두면 실행하지 않는다.
 * @EnableScheduling이 있어야 스케줄러가 등록되고 주기 실행이 시작된다.
 *
 * test 프로필에서 빼는 이유: 예약 작업이 켜져 있으면 테스트가 스프링 컨텍스트를 띄우는 것만으로 공공데이터 API를 실제로 호출한다.
 * 테스트 결과가 외부 서비스 상태와 네트워크에 좌우돼서는 안 되고 응답을 기다리는 시간만큼 CI도 지연된다.
 */
@Configuration
@Profile("!test")
@EnableScheduling
public class SchedulingConfig {
}