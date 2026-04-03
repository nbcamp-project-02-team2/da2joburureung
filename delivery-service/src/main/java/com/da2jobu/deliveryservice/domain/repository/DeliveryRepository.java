package com.da2jobu.deliveryservice.domain.repository;

import com.da2jobu.deliveryservice.domain.entity.Delivery;
import com.da2jobu.deliveryservice.domain.vo.DeliveryStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DeliveryRepository extends JpaRepository<Delivery, UUID> {

    Optional<Delivery> findByDeliveryIdAndDeletedAtIsNull(UUID deliveryId);

    Page<Delivery> findAllByDeletedAtIsNull(Pageable pageable);

    Page<Delivery> findByOrderIdAndDeletedAtIsNull(UUID orderId, Pageable pageable);

    Page<Delivery> findByStatusAndDeletedAtIsNull(DeliveryStatus status, Pageable pageable);

    Page<Delivery> findByOriginHubIdAndDeletedAtIsNull(UUID originHubId, Pageable pageable);

    Page<Delivery> findByDestinationHubIdAndDeletedAtIsNull(UUID destinationHubId, Pageable pageable);

    boolean existsByOrderIdAndDeletedAtIsNull(UUID orderId);
}
