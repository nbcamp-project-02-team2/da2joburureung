package com.da2jobu.deliveryservice.infrastructure.deliveryManager.config;

import com.da2jobu.deliveryservice.domain.deliveryManager.repository.DeliveryAssignmentRepository;
import com.da2jobu.deliveryservice.domain.deliveryManager.repository.DeliveryManagerRepository;
import com.da2jobu.deliveryservice.infrastructure.deliveryManager.persistence.DeliveryAssignmentRepositoryAdapter;
import com.da2jobu.deliveryservice.infrastructure.deliveryManager.persistence.DeliveryManagerRepositoryAdapter;
import com.da2jobu.deliveryservice.infrastructure.deliveryManager.persistence.JpaDeliveryAssignmentRepository;
import com.da2jobu.deliveryservice.infrastructure.deliveryManager.persistence.JpaDeliveryManagerRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RepositoryConfig {
    @Bean
    public DeliveryManagerRepository deliveryManagerRepository(
            JpaDeliveryManagerRepository jpaDeliveryManagerRepository,
            JPAQueryFactory jpaQueryFactory
    ) {
        return new DeliveryManagerRepositoryAdapter(jpaDeliveryManagerRepository, jpaQueryFactory);
    }

    @Bean
    public DeliveryAssignmentRepository deliveryAssignmentRepository(
            JpaDeliveryAssignmentRepository jpaDeliveryAssignmentRepository
    ) {
        return new DeliveryAssignmentRepositoryAdapter(jpaDeliveryAssignmentRepository);
    }
}
