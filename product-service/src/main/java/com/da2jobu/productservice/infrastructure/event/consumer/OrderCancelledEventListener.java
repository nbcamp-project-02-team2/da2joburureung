package com.da2jobu.productservice.infrastructure.event.consumer;

import com.da2jobu.productservice.application.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCancelledEventListener {

    private final ProductService productService;
    private final StringRedisTemplate redisTemplate;

    @KafkaListener(topics = "order.cancelled.v1", groupId = "product-service")
    public void consume(OrderCancelledEvent event) {
        log.info("OrderCancelledEvent 수신 - orderId={}, productId={}, quantity={}",
                event.orderId(), event.productId(), event.quantity());

        // 멱등성 보장: 동일 orderId 이벤트 중복 처리 방지
        String idempotencyKey = "order-cancelled:processed:" + event.orderId();
        Boolean isNew = redisTemplate.opsForValue().setIfAbsent(idempotencyKey, "1", Duration.ofDays(7));
        if (Boolean.FALSE.equals(isNew)) {
            log.warn("OrderCancelledEvent 중복 수신 스킵 - orderId={}", event.orderId());
            return;
        }

        try {
            productService.restoreStock(event.productId(), event.quantity());
            log.info("OrderCancelledEvent 처리 완료 - 재고 복구: productId={}, quantity={}",
                    event.productId(), event.quantity());
        } catch (Exception e) {
            log.error("OrderCancelledEvent 처리 실패 - orderId={}", event.orderId(), e);
            redisTemplate.delete(idempotencyKey); // 실패 시 키 삭제하여 재처리 허용
            throw e;
        }
    }
}
