package com.da2jobu.infrastructure.messaging.outbox;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxEventScheduler {

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Scheduled(fixedDelay = 10000)
    public void publishPendingEvents() {
        List<OutboxEvent> pendingEvents =
                outboxEventRepository.findByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING);

        if (pendingEvents.isEmpty()) {
            return;
        }

        log.debug("아웃박스 이벤트 처리 시작: {}건", pendingEvents.size());

        for (OutboxEvent event : pendingEvents) {
            try {
                kafkaTemplate.send(event.getTopic(), event.getAggregateId().toString(), event.getPayload())
                        .get();
                event.markPublished();
                outboxEventRepository.save(event);
                log.info("아웃박스 이벤트 발행 완료: id={}, topic={}", event.getEventId(), event.getTopic());
            } catch (Exception e) {
                log.warn("아웃박스 이벤트 발행 실패, 다음 주기에 재시도: id={}, cause={}", event.getEventId(), e.getMessage());
            }
        }
    }
}