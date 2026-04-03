package com.da2jobu.deliverymanagerservice.infrastructure.config;

import com.da2jobu.deliverymanagerservice.domain.repository.DeliveryManagerRepository;
import com.da2jobu.deliverymanagerservice.domain.service.DeliveryManagerDomainService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DomainServiceConfig {
    @Bean
    public DeliveryManagerDomainService deliveryManagerDomainService(DeliveryManagerRepository deliveryManagerRepository) {
        return new DeliveryManagerDomainService(deliveryManagerRepository);
    }
}
