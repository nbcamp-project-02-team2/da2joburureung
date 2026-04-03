package com.da2jobu.infrastructure.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class ResilienceConfig {

    private final CircuitBreakerRegistry circuitBreakerRegistry;

    @PostConstruct
    public void registerEventListeners() {
        circuitBreakerRegistry.getAllCircuitBreakers()
                .forEach(this::addEventListeners);

        // 나중에 동적으로 생성되는 CB도 감지
        circuitBreakerRegistry.getEventPublisher()
                .onEntryAdded(entry -> addEventListeners(entry.getAddedEntry()));
    }

    private void addEventListeners(CircuitBreaker cb) {
        String name = cb.getName();

        cb.getEventPublisher()
                .onStateTransition(event -> log.warn(
                        "CB 상태 변경: name={}, {} → {}",
                        name,
                        event.getStateTransition().getFromState(),
                        event.getStateTransition().getToState()
                ))
                .onCallNotPermitted(event -> log.warn(
                        "CB 호출 차단됨 (OPEN 상태): name={}", name
                ))
                .onError(event -> log.error(
                        "CB 오류 기록: name={}, cause={}",
                        name, event.getThrowable().getMessage()
                ))
                .onSlowCallRateExceeded(event -> log.warn(
                        "CB 느린 호출 비율 초과: name={}, rate={}%",
                        name, event.getSlowCallRate()
                ));
    }
}