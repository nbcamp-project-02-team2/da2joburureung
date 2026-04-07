package com.da2jobu.notificationservice.infrastructure.client;

import com.da2jobu.notificationservice.infrastructure.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@FeignClient(
        name = "slack-client",
        url = "${slack.webhook.url}",
        configuration = FeignConfig.class
)
public interface SlackClient {

    @PostMapping
    void send(List<String> ids, String message);
}
