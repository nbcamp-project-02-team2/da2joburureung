package com.da2jobu.deliveryservice.infrastructure.config;

import com.da2jobu.deliveryservice.domain.deliveryManager.repository.DeliveryManagerRepository;
import com.da2jobu.deliveryservice.domain.deliveryManager.service.DeliveryManagerDomainService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DomainServiceConfig {
    @Bean
    public DeliveryManagerDomainService deliveryManagerDomainService(DeliveryManagerRepository deliveryManagerRepository) {
        return new DeliveryManagerDomainService(deliveryManagerRepository);
    }
}
