package com.da2jobu.deliveryservice.domain.deliveryRouteRecord.repository;

import com.da2jobu.deliveryservice.application.delivery.dto.TodayCompanyDeliveryRouteResponseDto;
import com.da2jobu.deliveryservice.domain.delivery.vo.DeliveryStatus;
import com.da2jobu.deliveryservice.domain.deliveryRouteRecord.entity.DeliveryRouteRecord;
import com.da2jobu.deliveryservice.domain.deliveryRouteRecord.vo.RouteLocationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DeliveryRouteRecordRepository extends JpaRepository<DeliveryRouteRecord, UUID> {

    Optional<DeliveryRouteRecord> findByDeliveryRouteRecordIdAndDeletedAtIsNull(UUID deliveryRouteRecordId);

    List<DeliveryRouteRecord> findAllByDeliveryIdAndDeletedAtIsNullOrderBySequenceAsc(UUID deliveryId);

    // DELIVERY_MANAGER: 특정 배달에서 본인이 담당하는 경로가 있는지 확인
    boolean existsByDeliveryIdAndDeliveryManagerIdAndDeletedAtIsNull(UUID deliveryId, UUID deliveryManagerId);

    @Query("""
        select new com.da2jobu.deliveryservice.application.delivery.dto.TodayCompanyDeliveryRouteResponseDto(
            r.deliveryRouteRecordId,
            r.deliveryId,
            r.sequence,
            r.originId,
            r.originType,
            r.destinationId,
            r.destinationType,
            r.expectedDistanceKm,
            r.expectedDurationMin,
            r.realDistanceKm,
            r.realDurationMin,
            r.remainDurationMin,
            r.status,
            r.deliveryManagerId,
            d.desiredDeliveryAt
        )
        from DeliveryRouteRecord r, Delivery d
        where d.deliveryId = r.deliveryId
          and d.status = :deliveryStatus
          and d.desiredDeliveryAt >= :todayStart
          and d.desiredDeliveryAt < :tomorrowStart
          and r.originType = :originType
          and r.destinationType = :destinationType
          and r.originId = :hubId
          and d.deletedAt is null
          and r.deletedAt is null
        order by d.desiredDeliveryAt asc, r.sequence asc
    """)
    List<TodayCompanyDeliveryRouteResponseDto> findTodayCompanyDeliveryRoutesByHubId(
            @Param("hubId") UUID hubId,
            @Param("deliveryStatus") DeliveryStatus deliveryStatus,
            @Param("originType") RouteLocationType originType,
            @Param("destinationType") RouteLocationType destinationType,
            @Param("todayStart") LocalDateTime todayStart,
            @Param("tomorrowStart") LocalDateTime tomorrowStart
    );
}
