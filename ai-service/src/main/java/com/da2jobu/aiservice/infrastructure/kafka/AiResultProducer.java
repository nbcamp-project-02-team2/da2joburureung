package com.da2jobu.aiservice.infrastructure.kafka;

import com.da2jobu.aiservice.infrastructure.kafka.dto.AiDeliveryInfoEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiResultProducer {

    private final KafkaTemplate<String, AiDeliveryInfoEvent> kafkaTemplate;

    @Value("${kafka.producer.topics.ai-delivery-info}")
    private String topic;

    public void publishAiResult(AiDeliveryInfoEvent event) {
        kafkaTemplate.send(topic, event.deliveryId().toString(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("[AI-SERVICE] Kafka 발행 실패 - deliveryId:{}, error:{}", event.deliveryId(), ex.getMessage());
                    } else {
                        log.info("[AI-SERVICE] Kafka 발행 성공 - topic:{}, deliveryId:{}", topic, event.deliveryId());
                    }
                });
    }
}
