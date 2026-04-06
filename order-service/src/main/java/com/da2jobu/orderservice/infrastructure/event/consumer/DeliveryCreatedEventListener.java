package com.da2jobu.orderservice.infrastructure.event.consumer;

import com.da2jobu.orderservice.domain.model.Order;
import com.da2jobu.orderservice.domain.repository.OrderRepository;
import common.exception.CustomException;
import common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeliveryCreatedEventListener {

    private final OrderRepository orderRepository;

    @KafkaListener(topics = "delivery.created.v1", groupId = "order-service")
    @Transactional
    public void consume(DeliveryCreatedEvent event) {
        log.info("DeliveryCreatedEvent 수신 - orderId={}, deliveryId={}", event.orderId(), event.deliveryId());

        try {
            Order order = orderRepository.findById(event.orderId())
                    .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));
            order.assignDelivery(event.deliveryId());
            log.info("DeliveryCreatedEvent 처리 완료 - orderId={}", event.orderId());
        } catch (Exception e) {
            log.error("DeliveryCreatedEvent 처리 실패 - orderId={}", event.orderId(), e);
            throw e;
        }
    }
}
