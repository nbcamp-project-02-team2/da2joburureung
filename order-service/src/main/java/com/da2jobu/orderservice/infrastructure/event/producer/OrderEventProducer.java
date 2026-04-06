package com.da2jobu.orderservice.infrastructure.event.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventProducer {

    private static final String ORDER_ACCEPTED_TOPIC = "order.accepted.v1";
    private static final String ORDER_CANCELLED_TOPIC = "order.cancelled.v1";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishOrderAccepted(OrderAcceptedEvent event) {
        log.info("OrderAcceptedEvent 발행 - orderId={}", event.orderId());
        kafkaTemplate.send(ORDER_ACCEPTED_TOPIC, event.orderId().toString(), event);
    }

    public void publishOrderCancelled(OrderCancelledEvent event) {
        log.info("OrderCancelledEvent 발행 - orderId={}", event.orderId());
        kafkaTemplate.send(ORDER_CANCELLED_TOPIC, event.orderId().toString(), event);
    }
}
