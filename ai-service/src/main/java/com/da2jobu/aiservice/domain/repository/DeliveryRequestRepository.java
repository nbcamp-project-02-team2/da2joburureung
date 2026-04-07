package com.da2jobu.aiservice.domain.repository;

import com.da2jobu.aiservice.domain.model.DeliveryRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DeliveryRequestRepository extends JpaRepository<DeliveryRequest, UUID> {
}
