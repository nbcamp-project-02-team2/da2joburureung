package com.da2jobu.aiservice.infrastructure.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    @Value("${kafka.producer.topics.ai-delivery-info}")
    private String aiDeliveryInfoTopic;

    @Bean
    public NewTopic aiDeliveryInfoTopic() {
        return TopicBuilder.name(aiDeliveryInfoTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
