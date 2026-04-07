package com.da2jobu.deliveryservice.domain.delivery.repository;

import com.da2jobu.deliveryservice.domain.delivery.entity.Delivery;
import com.da2jobu.deliveryservice.domain.delivery.vo.DeliveryStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    // HUB_MANAGER: 담당 허브가 출발 또는 도착 허브인 배송 목록
    @Query("SELECT d FROM Delivery d WHERE (d.originHubId = :hubId OR d.destinationHubId = :hubId) AND d.deletedAt IS NULL")
    Page<Delivery> findByHubRelatedAndDeletedAtIsNull(@Param("hubId") UUID hubId, Pageable pageable);

    // DELIVERY_MANAGER: companyDeliveryManagerId 또는 경로 레코드의 deliveryManagerId로 배정된 배송 목록
    // (허브 배송 담당자와 업체 배송 담당자 모두 커버)
    @Query(value = """
            SELECT DISTINCT d FROM Delivery d
            WHERE d.deletedAt IS NULL
              AND (d.companyDeliveryManagerId = :managerId
                   OR EXISTS (
                       SELECT 1 FROM DeliveryRouteRecord r
                       WHERE r.deliveryId = d.deliveryId
                         AND r.deliveryManagerId = :managerId
                         AND r.deletedAt IS NULL
                   ))
            """,
           countQuery = """
            SELECT COUNT(DISTINCT d) FROM Delivery d
            WHERE d.deletedAt IS NULL
              AND (d.companyDeliveryManagerId = :managerId
                   OR EXISTS (
                       SELECT 1 FROM DeliveryRouteRecord r
                       WHERE r.deliveryId = d.deliveryId
                         AND r.deliveryManagerId = :managerId
                         AND r.deletedAt IS NULL
                   ))
            """)
    Page<Delivery> findByManagerRelatedAndDeletedAtIsNull(@Param("managerId") UUID managerId, Pageable pageable);

    // COMPANY_MANAGER: 발송 업체(supplierCompanyId) 또는 수령 업체(receiverCompanyId)가 본인 업체인 배송 목록
    @Query("SELECT d FROM Delivery d WHERE (d.supplierCompanyId = :companyId OR d.receiverCompanyId = :companyId) AND d.deletedAt IS NULL")
    Page<Delivery> findByCompanyRelatedAndDeletedAtIsNull(@Param("companyId") UUID companyId, Pageable pageable);
}
