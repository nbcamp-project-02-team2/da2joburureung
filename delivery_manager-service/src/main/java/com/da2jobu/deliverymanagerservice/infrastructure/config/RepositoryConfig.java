package com.da2jobu.deliverymanagerservice.infrastructure.config;

import com.da2jobu.deliverymanagerservice.domain.repository.DeliveryAssignmentRepository;
import com.da2jobu.deliverymanagerservice.domain.repository.DeliveryManagerRepository;
import com.da2jobu.deliverymanagerservice.infrastructure.persistence.DeliveryAssignmentRepositoryAdapter;
import com.da2jobu.deliverymanagerservice.infrastructure.persistence.DeliveryManagerRepositoryAdapter;
import com.da2jobu.deliverymanagerservice.infrastructure.persistence.JpaDeliveryAssignmentRepository;
import com.da2jobu.deliverymanagerservice.infrastructure.persistence.JpaDeliveryManagerRepository;
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
