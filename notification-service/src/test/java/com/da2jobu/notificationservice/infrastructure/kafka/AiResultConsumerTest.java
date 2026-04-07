package com.da2jobu.notificationservice.infrastructure.kafka;

import com.da2jobu.notificationservice.domain.model.MessageSend;
import com.da2jobu.notificationservice.domain.model.MessageStatus;
import com.da2jobu.notificationservice.domain.model.SlackMessage;
import com.da2jobu.notificationservice.domain.repository.SlackMessageRepository;
import com.da2jobu.notificationservice.infrastructure.kafka.dto.AiDeliveryInfoEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class AiResultConsumerTest {

    @InjectMocks
    private AiResultConsumer aiResultConsumer;

    @Mock private MessageSend messageSend;
    @Mock private SlackMessageRepository slackMessageRepository;

    @Test
    @DisplayName("AI 결과 이벤트를 수신하면 각 수신자에게 Slack 메시지를 발송하고 이력을 저장한다")
    void onAiDeliveryInfo_sendsSlackAndSavesHistory() {
        // given
        AiDeliveryInfoEvent event = new AiDeliveryInfoEvent(
                UUID.randomUUID(),
                List.of("U_DRIVER", "U_HUB"),
                LocalDateTime.of(2026, 4, 2, 14, 30),
                "서울 허브 → 부산 허브, 경부고속도로 경유",
                "부산 도착지 오후 비 예보. 빗길 서행 권장."
        );

        // void 메서드 모킹은 willDoNothing() 사용
        willDoNothing().given(messageSend).send(any(), any());
        given(slackMessageRepository.save(any())).willReturn(mock(SlackMessage.class));

        // when
        aiResultConsumer.onAiDeliveryInfo(event);

        // then
        then(messageSend).should(times(2)).send(any(), any());

        ArgumentCaptor<SlackMessage> captor = ArgumentCaptor.forClass(SlackMessage.class);
        then(slackMessageRepository).should(times(2)).save(captor.capture());

        List<SlackMessage> saved = captor.getAllValues();
        assertThat(saved).hasSize(2);
        assertThat(saved).allMatch(m -> m.getMessageStatus() == MessageStatus.SUCCESS);
    }

    @Test
    @DisplayName("Slack 발송 예외 발생 시 FAIL 상태로 이력을 저장한다")
    void onAiDeliveryInfo_slackException_savesFailStatus() {
        // given
        AiDeliveryInfoEvent event = new AiDeliveryInfoEvent(
                UUID.randomUUID(),
                List.of("U_DRIVER"),
                LocalDateTime.now(),
                "서울 → 부산", "운행 주의"
        );

        // send()에서 예외 발생 모킹
        willThrow(new RuntimeException("Slack API 오류")).given(messageSend).send(any(), any());
        given(slackMessageRepository.save(any())).willReturn(mock(SlackMessage.class));

        // when
        aiResultConsumer.onAiDeliveryInfo(event);

        // then
        ArgumentCaptor<SlackMessage> captor = ArgumentCaptor.forClass(SlackMessage.class);
        then(slackMessageRepository).should(times(1)).save(captor.capture());
        assertThat(captor.getValue().getMessageStatus()).isEqualTo(MessageStatus.FAIL);
    }
}
