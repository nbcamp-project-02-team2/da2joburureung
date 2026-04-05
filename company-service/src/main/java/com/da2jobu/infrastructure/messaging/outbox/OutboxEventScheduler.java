package com.da2jobu.infrastructure.messaging.outbox;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxEventScheduler {

    private static final int MAX_RETRY = 3;

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Scheduled(fixedDelay = 10000)
    @Transactional
    public void publishPendingEvents() {
        List<OutboxEvent> pendingEvents =
                outboxEventRepository.findByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING);

        if (pendingEvents.isEmpty()) {
            return;
        }

        log.debug("미발행 아웃박스 이벤트 처리 시작: {}건", pendingEvents.size());

        for (OutboxEvent event : pendingEvents) {
            try {
                kafkaTemplate.send(event.getTopic(), event.getAggregateId().toString(), event.getPayload())
                        .get();
                event.markPublished();
                log.info("아웃박스 이벤트 발행 완료: id={}, topic={}, retryCount={}",
                        event.getEventId(), event.getTopic(), event.getRetryCount());
            } catch (Exception e) {
                event.incrementRetry(MAX_RETRY);
                if (event.getStatus() == OutboxStatus.FAILED) {
                    log.error("아웃박스 이벤트 최대 재시도 초과, FAILED 처리: id={}, retryCount={}",
                            event.getEventId(), event.getRetryCount());
                } else {
                    log.warn("아웃박스 이벤트 발행 실패, 재시도 예정: id={}, retryCount={}/{}",
                            event.getEventId(), event.getRetryCount(), MAX_RETRY);
                }
            }
        }
    }
}