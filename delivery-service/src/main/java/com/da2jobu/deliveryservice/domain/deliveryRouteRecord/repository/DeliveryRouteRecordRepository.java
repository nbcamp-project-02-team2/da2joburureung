package com.da2jobu.deliveryservice.domain.deliveryRouteRecord.repository;

import com.da2jobu.deliveryservice.domain.deliveryRouteRecord.entity.DeliveryRouteRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DeliveryRouteRecordRepository extends JpaRepository<DeliveryRouteRecord, UUID> {

    Optional<DeliveryRouteRecord> findByDeliveryRouteRecordIdAndDeletedAtIsNull(UUID deliveryRouteRecordId);

    List<DeliveryRouteRecord> findAllByDeliveryIdAndDeletedAtIsNullOrderBySequenceAsc(UUID deliveryId);
}
