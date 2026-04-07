package com.da2jobu.aiservice.application.service;

import com.da2jobu.aiservice.domain.model.DeliveryAiResult;
import com.da2jobu.aiservice.domain.model.DeliveryRequest;
import com.da2jobu.aiservice.domain.repository.DeliveryAiResultRepository;
import com.da2jobu.aiservice.domain.repository.DeliveryRequestRepository;
import com.da2jobu.aiservice.infrastructure.kafka.AiResultProducer;
import com.da2jobu.aiservice.infrastructure.kafka.dto.AiDeliveryInfoEvent;
import com.da2jobu.aiservice.infrastructure.kafka.dto.DeliveryConfirmedEvent;
import com.da2jobu.aiservice.infrastructure.openai.OpenAiDeliveryAdvisor;
import com.da2jobu.aiservice.infrastructure.weather.WeatherClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.embedding.EmbeddingModel;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class AiDeliveryServiceTest {

    @InjectMocks
    private AiDeliveryService aiDeliveryService;

    @Mock private DeliveryRequestRepository deliveryRequestRepository;
    @Mock private DeliveryAiResultRepository deliveryAiResultRepository;
    @Mock private WeatherClient weatherClient;
    @Mock private OpenAiDeliveryAdvisor openAiDeliveryAdvisor;
    @Mock private AiResultProducer aiResultProducer;
    @Mock private EmbeddingModel embeddingModel;

    @Test
    @DisplayName("배송 확정 이벤트를 처리하면 AI 결과를 저장하고 Kafka에 발행한다")
    void processDelivery_success() {
        // given
        UUID deliveryId = UUID.randomUUID();
        DeliveryConfirmedEvent event = new DeliveryConfirmedEvent(
                deliveryId, "U_DRIVER", "U_HUB",
                "서울 허브", "부산 허브",
                37.5665, 126.9780, 35.1796, 129.0756,
                LocalDateTime.of(2026, 4, 2, 9, 0)
        );

        given(deliveryRequestRepository.save(any())).willReturn(mock(DeliveryRequest.class));
        given(weatherClient.getWeatherDescription(anyDouble(), anyDouble()))
                .willReturn("맑음, 기온 15.0°C, 습도 40%, 풍속 2.0m/s");
        given(embeddingModel.embed(anyString())).willReturn(new float[]{0.1f, 0.2f, 0.3f});
        given(deliveryAiResultRepository.findSimilarResults(anyString(), anyInt())).willReturn(List.of());

        OpenAiDeliveryAdvisor.AiDeliveryResult aiResult = new OpenAiDeliveryAdvisor.AiDeliveryResult(
                "서울 허브 → 부산 허브, 경부고속도로 경유",
                LocalDateTime.of(2026, 4, 2, 14, 30),
                "맑은 날씨이나 졸음운전에 유의하세요."
        );
        given(openAiDeliveryAdvisor.generateDeliveryInfo(any(), any(), any(), any(), any(), any(), any()))
                .willReturn(aiResult);

        DeliveryAiResult savedResult = mock(DeliveryAiResult.class);
        given(savedResult.getResultId()).willReturn(UUID.randomUUID());
        given(deliveryAiResultRepository.save(any())).willReturn(savedResult);

        // when
        aiDeliveryService.processDelivery(event);

        // then
        then(deliveryRequestRepository).should(times(1)).save(any(DeliveryRequest.class));
        then(deliveryAiResultRepository).should(times(1)).save(any(DeliveryAiResult.class));

        ArgumentCaptor<AiDeliveryInfoEvent> kafkaCaptor = ArgumentCaptor.forClass(AiDeliveryInfoEvent.class);
        then(aiResultProducer).should(times(1)).publishAiResult(kafkaCaptor.capture());

        AiDeliveryInfoEvent published = kafkaCaptor.getValue();
        assertThat(published.deliveryId()).isEqualTo(deliveryId);
        assertThat(published.slackIds()).containsExactly("U_DRIVER", "U_HUB");
        assertThat(published.routeSummary()).isEqualTo(aiResult.routeSummary());
        assertThat(published.weatherSafetyComment()).isEqualTo(aiResult.weatherSafetyComment());
    }

    @Test
    @DisplayName("날씨 조회 실패 시 기본 메시지를 사용하고 처리를 완료한다")
    void processDelivery_weatherFail_continuesProcessing() {
        // given
        DeliveryConfirmedEvent event = new DeliveryConfirmedEvent(
                UUID.randomUUID(), "U_DRIVER", "U_HUB",
                "서울 허브", "부산 허브",
                37.5665, 126.9780, 35.1796, 129.0756,
                LocalDateTime.now()
        );

        given(deliveryRequestRepository.save(any())).willReturn(mock(DeliveryRequest.class));
        given(weatherClient.getWeatherDescription(anyDouble(), anyDouble()))
                .willReturn("날씨 정보를 가져올 수 없습니다");
        given(embeddingModel.embed(anyString())).willReturn(new float[]{0.1f, 0.2f});
        given(deliveryAiResultRepository.findSimilarResults(anyString(), anyInt())).willReturn(List.of());
        given(openAiDeliveryAdvisor.generateDeliveryInfo(any(), any(), any(), any(), any(), any(), any()))
                .willReturn(new OpenAiDeliveryAdvisor.AiDeliveryResult(
                        "경로 요약", LocalDateTime.now().plusHours(5), "안전 운행하세요."));

        DeliveryAiResult savedResult = mock(DeliveryAiResult.class);
        given(savedResult.getResultId()).willReturn(UUID.randomUUID());
        given(deliveryAiResultRepository.save(any())).willReturn(savedResult);

        // when & then (예외 없이 완료)
        aiDeliveryService.processDelivery(event);
        then(aiResultProducer).should(times(1)).publishAiResult(any());
    }
}
