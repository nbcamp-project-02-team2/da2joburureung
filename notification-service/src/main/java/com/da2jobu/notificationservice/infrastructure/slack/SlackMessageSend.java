package com.da2jobu.notificationservice.infrastructure.slack;

import com.da2jobu.notificationservice.domain.model.MessageSend;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
//@RefreshScope
public class SlackMessageSend implements MessageSend {

    @Value("${slack.token}")
    private String TOKEN;
    @Value("${slack.baseUrl}")
    private String SLACK_BASE_URL;
    @Value("${slack.conversationOpenUrl}")
    private String SLACK_CONVERSATION_OPEN_URL;
    @Value("${slack.chatPostMessageUrl}")
    private String SLACK_CHAT_POST_MESSAGE_URL;

    @Override
    public void send(List<String> ids, String message) {
        if (ids != null) {

            RestClient client = RestClient.builder()
                    .baseUrl(SLACK_BASE_URL)
                    .build();
            // Channel ID 처리 S
            ResponseEntity<JsonNode> response = client.post()
                    .uri(SLACK_CONVERSATION_OPEN_URL)
                    .header("Authorization", "Bearer " + TOKEN)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("users", String.join(",", ids)))
                    .retrieve()
                    .toEntity(JsonNode.class);
            JsonNode node = response.getBody();

            if (!response.getStatusCode().is2xxSuccessful() ||  node.get("ok") == null || !node.get("ok").toString().equals("true")) return;

            String channelId = node.get("channel").get("id").textValue();
            // Channel ID 처리 E

            // 메세지 발송 처리 S
            response = client.post()
                    .uri(SLACK_CHAT_POST_MESSAGE_URL)
                    .header("Authorization", "Bearer " + TOKEN)
                    .body(Map.of("channel", channelId, "text", message, "as_user", true))
                    .contentType(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .toEntity(JsonNode.class);

            if (response.getStatusCode().is2xxSuccessful() && node.get("ok") != null) {
                node.get("ok").toString();
            }
            // 메세지 발송 처리 E
        }


    }
}
