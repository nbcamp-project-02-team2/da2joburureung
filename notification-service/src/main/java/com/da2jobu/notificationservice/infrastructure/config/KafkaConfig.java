package com.da2jobu.notificationservice.infrastructure.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic aiDeliveryInfoTopic() {
        return TopicBuilder.name("ai.delivery.info.generated")
                .partitions(3)
                .replicas(1)
                .build();
    }
}
