package com.da2jobu.deliveryservice.delivery.repository;

import com.da2jobu.deliveryservice.delivery.entity.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DeliveryRepository extends JpaRepository<Delivery, UUID> {

}
