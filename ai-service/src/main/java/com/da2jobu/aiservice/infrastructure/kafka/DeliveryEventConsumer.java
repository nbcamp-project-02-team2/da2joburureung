package com.da2jobu.aiservice.infrastructure.kafka;

import com.da2jobu.aiservice.application.service.AiDeliveryService;
import com.da2jobu.aiservice.infrastructure.kafka.dto.DeliveryConfirmedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeliveryEventConsumer {

    private final AiDeliveryService aiDeliveryService;

    @KafkaListener(
            topics = "${kafka.consumer.topics.delivery-prepared}",
            groupId = "${kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onDeliveryConfirmed(DeliveryConfirmedEvent event) {
        log.info("[AI-SERVICE] delivery.prepared 수신 - deliveryId: {}", event.deliveryId());
        try {
            aiDeliveryService.processDelivery(event);
        } catch (Exception e) {
            log.error("[AI-SERVICE] 배송 AI 처리 실패 - deliveryId: {}", event.deliveryId(), e);
        }
    }
}
