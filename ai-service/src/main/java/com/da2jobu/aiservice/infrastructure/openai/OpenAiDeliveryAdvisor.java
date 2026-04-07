package com.da2jobu.aiservice.infrastructure.openai;

import com.da2jobu.aiservice.domain.model.DeliveryAiResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class OpenAiDeliveryAdvisor {

    private final ChatClient chatClient;

    /**
     * GPT-4o로 배송 ETA 및 안전 운행 코멘트 생성
     */
    public AiDeliveryResult generateDeliveryInfo(
            UUID deliveryId,
            String departureHubName,
            String arrivalHubName,
            LocalDateTime scheduledDeparture,
            String departureWeather,
            String arrivalWeather,
            String similarCasesContext
    ) {
        String systemPrompt = """
                당신은 B2B 물류 배송 전문 AI 어시스턴트입니다.
                배송 담당자와 허브 담당자에게 유용한 정보를 제공합니다.
                반드시 아래 형식으로만 응답하세요:
                
                [경로요약]
                {출발지에서 도착지까지 예상 경로를 1~2문장으로 요약}
                
                [도착예정시간]
                {출발 예정 시간 기준 예상 소요시간과 도착 예정 시각, 형식: YYYY-MM-DDTHH:MM:SS}
                
                [안전운행코멘트]
                {날씨 조건을 반영한 안전 운행 유의사항 2~3문장}
                """;

        String userPrompt = """
                배송 정보:
                - 배송 ID: %s
                - 출발 허브: %s
                - 도착 허브: %s
                - 출발 예정 시각: %s
                - 출발지 날씨: %s
                - 도착지 날씨: %s
                
                유사 과거 배송 사례:
                %s
                """.formatted(deliveryId, departureHubName, arrivalHubName,
                scheduledDeparture, departureWeather, arrivalWeather, similarCasesContext);

        String response = chatClient.prompt(
                        new Prompt(List.of(new SystemMessage(systemPrompt), new UserMessage(userPrompt))))
                .call()
                .content();

        return parseResponse(response, scheduledDeparture);
    }

    private AiDeliveryResult parseResponse(String response, LocalDateTime scheduledDeparture) {
        String routeSummary = extractSection(response, "[경로요약]", "[도착예정시간]");
        String etaStr = extractSection(response, "[도착예정시간]", "[안전운행코멘트]").trim();
        String safetyComment = extractSection(response, "[안전운행코멘트]", null);

        LocalDateTime eta;
        try {
            eta = LocalDateTime.parse(etaStr.lines()
                    .map(String::trim)
                    .filter(l -> l.matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}"))
                    .findFirst()
                    .orElse(scheduledDeparture.plusHours(5).toString()));
        } catch (Exception e) {
            eta = scheduledDeparture.plusHours(5);
        }

        return new AiDeliveryResult(routeSummary.trim(), eta, safetyComment.trim());
    }

    private String extractSection(String text, String startTag, String endTag) {
        int start = text.indexOf(startTag);
        if (start < 0) return "";
        start += startTag.length();
        if (endTag == null) return text.substring(start);
        int end = text.indexOf(endTag, start);
        return end < 0 ? text.substring(start) : text.substring(start, end);
    }

    public record AiDeliveryResult(String routeSummary, LocalDateTime eta, String weatherSafetyComment) {}
}
