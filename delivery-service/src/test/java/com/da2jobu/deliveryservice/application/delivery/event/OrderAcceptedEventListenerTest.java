package com.da2jobu.deliveryservice.application.delivery.event;

import com.da2jobu.deliveryservice.application.delivery.command.CreateDeliveryFromOrderCommand;
import com.da2jobu.deliveryservice.application.delivery.dto.OrderAcceptedEvent;
import com.da2jobu.deliveryservice.application.delivery.service.CreateDeliveryFromOrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.internal.verification.VerificationModeFactory.times;

class OrderAcceptedEventListenerTest {

    @Mock
    private CreateDeliveryFromOrderService createDeliveryFromOrderService;

    @InjectMocks
    private OrderAcceptedEventListener orderAcceptedEventListener;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("OrderAcceptedEvent 수신 시 CreateDeliveryFromOrderCommand로 변환 후 서비스 호출")
    void consume_success() {
        // given
        UUID orderId = UUID.randomUUID();
        UUID supplierId = UUID.randomUUID();
        UUID receiverId = UUID.randomUUID();
        LocalDateTime desiredDeliveryAt = LocalDateTime.of(2026, 4, 5, 15, 0);

        OrderAcceptedEvent event = new OrderAcceptedEvent(
                orderId,
                supplierId,
                receiverId,
                "문 앞에 놔주세요",
                "receiver_manager",
                desiredDeliveryAt
        );

        // when
        orderAcceptedEventListener.consume(event);

        // then
        ArgumentCaptor<CreateDeliveryFromOrderCommand> captor =
                ArgumentCaptor.forClass(CreateDeliveryFromOrderCommand.class);

        verify(createDeliveryFromOrderService, times(1)).execute(captor.capture());

        CreateDeliveryFromOrderCommand command = captor.getValue();

        assertThat(command.orderId()).isEqualTo(orderId);
        assertThat(command.supplierId()).isEqualTo(supplierId);
        assertThat(command.receiverId()).isEqualTo(receiverId);
        assertThat(command.requirements()).isEqualTo("문 앞에 놔주세요");
        assertThat(command.createdBy()).isEqualTo("receiver_manager");
        assertThat(command.desiredDeliveryAt()).isEqualTo(desiredDeliveryAt);
    }

    @Test
    @DisplayName("서비스 호출 중 예외가 발생하면 예외를 다시 던진다")
    void consume_fail_when_service_throws() {
        // given
        OrderAcceptedEvent event = new OrderAcceptedEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                "요청사항",
                "receiver_manager",
                LocalDateTime.of(2026, 4, 5, 15, 0)
        );

        doThrow(new RuntimeException("서비스 실패"))
                .when(createDeliveryFromOrderService)
                .execute(any(CreateDeliveryFromOrderCommand.class));

        // when & then
        org.junit.jupiter.api.Assertions.assertThrows(
                RuntimeException.class,
                () -> orderAcceptedEventListener.consume(event)
        );
    }
}