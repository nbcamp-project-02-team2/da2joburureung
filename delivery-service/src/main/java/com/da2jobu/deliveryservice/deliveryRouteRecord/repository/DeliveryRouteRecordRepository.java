package com.da2jobu.deliveryservice.deliveryRouteRecord.repository;

import com.da2jobu.deliveryservice.deliveryRouteRecord.entity.DeliveryRouteRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DeliveryRouteRecordRepository extends JpaRepository<DeliveryRouteRecord, UUID> {

}
