package com.da2jobu.aiservice.application.service;

import com.da2jobu.aiservice.domain.model.DeliveryAiResult;
import com.da2jobu.aiservice.domain.model.DeliveryRequest;
import com.da2jobu.aiservice.domain.repository.DeliveryAiResultRepository;
import com.da2jobu.aiservice.domain.repository.DeliveryRequestRepository;
import com.da2jobu.aiservice.infrastructure.client.ProductClient;
import com.da2jobu.aiservice.infrastructure.kafka.AiResultProducer;
import com.da2jobu.aiservice.infrastructure.kafka.dto.AiDeliveryInfoEvent;
import com.da2jobu.aiservice.infrastructure.kafka.dto.DeliveryConfirmedEvent;
import com.da2jobu.aiservice.infrastructure.openai.OpenAiDeliveryAdvisor;
import com.da2jobu.aiservice.infrastructure.weather.WeatherClient;
import com.da2jobu.aiservice.interfaces.controller.dto.AiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiDeliveryService {

    private final DeliveryRequestRepository deliveryRequestRepository;
    private final DeliveryAiResultRepository deliveryAiResultRepository;
    private final WeatherClient weatherClient;
    private final ProductClient productClient;
    private final OpenAiDeliveryAdvisor openAiDeliveryAdvisor;
    private final AiResultProducer aiResultProducer;
    private final EmbeddingModel embeddingModel;

    @Transactional
    public void processDelivery(DeliveryConfirmedEvent event) {
        // 1. 입력 저장 (ai_input 스키마)
        DeliveryRequest request = DeliveryRequest.builder()
                .deliveryId(event.deliveryId())
                .deliveryManagerSlackId(event.deliveryManagerSlackId())
                .hubManagerSlackId(event.hubManagerSlackId())
                .departureHubName(event.departureHubName())
                .productName(event.productName())
                .arrivalHubName(event.arrivalHubName())
                .departureLat(event.departureLat())
                .departureLon(event.departureLon())
                .arrivalLat(event.arrivalLat())
                .arrivalLon(event.arrivalLon())
                .scheduledDepartureTime(event.scheduledDepartureTime())
                .build();
        deliveryRequestRepository.save(request);
        log.info("[AI-SERVICE] 배송 요청 저장 완료 - deliveryId: {}", event.deliveryId());

        // 2. 날씨 조회
        String departureWeather = weatherClient.getWeatherDescription(event.departureLat(), event.departureLon());
        String arrivalWeather = weatherClient.getWeatherDescription(event.arrivalLat(), event.arrivalLon());
        log.info("[AI-SERVICE] 날씨 조회 완료 - 출발: {}, 도착: {}", departureWeather, arrivalWeather);

        // 3. RAG - 유사 배송 이력 검색
        String queryText = "%s에서 %s까지 배송".formatted(event.departureHubName(), event.arrivalHubName());
        float[] queryEmbedding = embeddingModel.embed(queryText);
        String embeddingStr = Arrays.toString(queryEmbedding);

        List<DeliveryAiResult> similarResults = deliveryAiResultRepository.findSimilarResults(embeddingStr, 3);
        String similarCasesContext = similarResults.isEmpty()
                ? "유사 사례 없음"
                : similarResults.stream()
                .map(r -> "- %s | ETA: %s | %s".formatted(r.getRouteSummary(), r.getEstimatedArrivalTime(), r.getWeatherSafetyComment()))
                .collect(Collectors.joining("\n"));

        // 4. GPT-4o 호출
        OpenAiDeliveryAdvisor.AiDeliveryResult aiResult = openAiDeliveryAdvisor.generateDeliveryInfo(
                event.deliveryId(),
                event.departureHubName(),
                event.arrivalHubName(),
                event.scheduledDepartureTime(),
                departureWeather,
                arrivalWeather,
                similarCasesContext
        );

        // 5. 결과 임베딩 생성 및 저장 (ai_output 스키마)
        String resultText = aiResult.routeSummary() + " " + aiResult.weatherSafetyComment();
        float[] resultEmbedding = embeddingModel.embed(resultText);

        DeliveryAiResult saved = deliveryAiResultRepository.save(
                DeliveryAiResult.builder()
                        .deliveryId(event.deliveryId())
                        .estimatedArrivalTime(aiResult.eta())
                        .routeSummary(aiResult.routeSummary())
                        .weatherSafetyComment(aiResult.weatherSafetyComment())
                        .embedding(resultEmbedding)
                        .build()
        );
        log.info("[AI-SERVICE] AI 결과 저장 완료 - resultId: {}", saved.getResultId());

        // 6. Kafka 발행 (ai.delivery.info.generated)
        aiResultProducer.publishAiResult(new AiDeliveryInfoEvent(
                event.deliveryId(),
                List.of(event.deliveryManagerSlackId(), event.hubManagerSlackId()),
                aiResult.eta(),
                aiResult.routeSummary(),
                aiResult.weatherSafetyComment()
        ));
    }

    @Transactional(readOnly = true)
    public AiResponse getAiHistory(int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<DeliveryAiResult> resultPage = deliveryAiResultRepository.findAll(pageRequest);

        List<AiResponse.AiResultDto> dtos = resultPage.getContent().stream()
                .map(r -> new AiResponse.AiResultDto(
                        r.getResultId(),
                        r.getDeliveryId(),
                        r.getEstimatedArrivalTime(),
                        r.getRouteSummary(),
                        r.getWeatherSafetyComment(),
                        r.getCreatedAt()
                ))
                .toList();

        return new AiResponse(
                dtos,
                resultPage.getNumber(),
                resultPage.getSize(),
                resultPage.getTotalElements(),
                resultPage.getTotalPages(),
                resultPage.isFirst(),
                resultPage.isLast()
        );
    }
}

