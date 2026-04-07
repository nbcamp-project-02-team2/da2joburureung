package com.da2jobu.notificationservice.infrastructure.kafka;

import com.da2jobu.notificationservice.domain.model.MessageStatus;
import com.da2jobu.notificationservice.domain.model.MessageType;
import com.da2jobu.notificationservice.domain.model.SlackMessage;
import com.da2jobu.notificationservice.domain.repository.SlackMessageRepository;
import com.da2jobu.notificationservice.infrastructure.kafka.dto.AiDeliveryInfoEvent;
import com.da2jobu.notificationservice.domain.model.MessageSend;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiResultConsumer {

    private final MessageSend messageSend;
    private final SlackMessageRepository slackMessageRepository;

    @KafkaListener(
            topics = "${kafka.consumer.topics.ai-delivery-info}",
            groupId = "${kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional
    public void onAiDeliveryInfo(AiDeliveryInfoEvent event) {
        log.info("[NOTIFICATION-SERVICE] ai.delivery.info.generated 수신 - deliveryId: {}", event.deliveryId());

        String message = buildSlackMessage(event);

        for (String slackId : event.slackIds()) {
            boolean sent = true;
            try {
                messageSend.send(List.of(slackId), message);
            } catch (Exception e) {
                sent = false;
                log.error("[NOTIFICATION-SERVICE] Slack 발송 실패 - slackId: {}, error: {}", slackId, e.getMessage());
            }

            // recipientId는 내부 식별용 UUID로 저장 (Slack ID는 String이므로 별도 UUID 생성)
            SlackMessage record = SlackMessage.create(
                    UUID.randomUUID(),
                    message,
                    MessageType.NOTIFICATION,
                    sent ? MessageStatus.SUCCESS : MessageStatus.FAIL
            );
            slackMessageRepository.save(record);
            log.info("[NOTIFICATION-SERVICE] 메시지 이력 저장 완료 - slackId: {}, status: {}", slackId, sent ? "SUCCESS" : "FAIL");
        }
    }

    private String buildSlackMessage(AiDeliveryInfoEvent event) {
        return """
                🚚 *배송 정보 알림*
                - 배송 ID: `%s`
                - 경로: %s
                - 도착 예정 시각: *%s*
                - 안전 운행 안내: %s
                """.formatted(
                event.deliveryId(),
                event.routeSummary(),
                event.estimatedArrivalTime(),
                event.weatherSafetyComment()
        );
    }
}
