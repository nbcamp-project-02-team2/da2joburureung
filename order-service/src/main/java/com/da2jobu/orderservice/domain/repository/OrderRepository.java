package com.da2jobu.orderservice.domain.repository;

import com.da2jobu.orderservice.domain.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID>, OrderRepositoryCustom {
}
