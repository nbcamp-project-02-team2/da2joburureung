package com.da2jobu.deliveryservice.infrastructure.delivery.messaging;

import com.da2jobu.deliveryservice.application.delivery.event.DeliveryPreparedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeliveryPreparedEventProducer {

    private static final String TOPIC = "delivery.prepared.v1";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publish(DeliveryPreparedEvent event) {
        kafkaTemplate.send(TOPIC, event.orderId().toString(), event);
        log.info("DeliveryPreparedEvent 발행 완료 - orderId={}, deliveryId={}",
                event.orderId(), event.deliveryId());
    }
}
