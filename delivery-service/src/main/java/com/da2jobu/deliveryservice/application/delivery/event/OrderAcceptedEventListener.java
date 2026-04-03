package com.da2jobu.deliveryservice.application.delivery.event;

import com.da2jobu.deliveryservice.application.delivery.command.CreateDeliveryFromOrderCommand;
import com.da2jobu.deliveryservice.application.delivery.dto.OrderAcceptedEvent;
import com.da2jobu.deliveryservice.application.delivery.service.CreateDeliveryFromOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderAcceptedEventListener {

    private final CreateDeliveryFromOrderService createDeliveryFromOrderService;

    @KafkaListener(topics = "order.accepted.v1", groupId = "delivery-service")
    public void consume(OrderAcceptedEvent event) {
        log.info("OrderAcceptedEvent 수신 - orderId={}", event.orderId());

        CreateDeliveryFromOrderCommand command = new CreateDeliveryFromOrderCommand(
                event.orderId(),
                event.supplierId(),
                event.receiverId(),
                event.requirements(),
                event.createdBy()
        );

        createDeliveryFromOrderService.execute(command);
    }
}
