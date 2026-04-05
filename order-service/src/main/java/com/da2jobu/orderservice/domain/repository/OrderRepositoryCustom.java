package com.da2jobu.orderservice.domain.repository;

import com.da2jobu.orderservice.domain.model.Order;
import com.da2jobu.orderservice.domain.model.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface OrderRepositoryCustom {

    Page<Order> searchOrders(UUID supplierId, UUID receiverId, UUID hubId,
                              OrderStatus status, Pageable pageable);

    long countActiveOrdersByCompanyId(UUID companyId);
}
