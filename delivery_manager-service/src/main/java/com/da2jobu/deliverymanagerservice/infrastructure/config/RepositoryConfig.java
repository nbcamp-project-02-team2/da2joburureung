package com.da2jobu.deliverymanagerservice.infrastructure.config;

import com.da2jobu.deliverymanagerservice.domain.repository.DeliveryManagerRepository;
import com.da2jobu.deliverymanagerservice.infrastructure.persistence.DeliveryManagerRepositoryAdapter;
import com.da2jobu.deliverymanagerservice.infrastructure.persistence.JpaDeliveryManagerRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RepositoryConfig {
    @Bean
    public DeliveryManagerRepository deliveryManagerRepository(JpaDeliveryManagerRepository jpaDeliveryManagerRepository) {
        return new DeliveryManagerRepositoryAdapter(jpaDeliveryManagerRepository);
    }

}
