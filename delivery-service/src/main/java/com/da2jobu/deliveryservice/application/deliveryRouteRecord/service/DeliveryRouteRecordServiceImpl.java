package com.da2jobu.deliveryservice.application.deliveryRouteRecord.service;

import com.da2jobu.deliveryservice.application.delivery.service.DeliveryPermissionChecker;
import com.da2jobu.deliveryservice.application.deliveryRouteRecord.command.CreateDeliveryRouteRecordsCommand;
import com.da2jobu.deliveryservice.application.deliveryRouteRecord.command.UpdateDeliveryRouteMetricsCommand;
import com.da2jobu.deliveryservice.application.deliveryRouteRecord.command.UpdateDeliveryRouteStatusCommand;
import com.da2jobu.deliveryservice.application.deliveryRouteRecord.dto.*;
import com.da2jobu.deliveryservice.domain.delivery.entity.Delivery;
import com.da2jobu.deliveryservice.domain.delivery.repository.DeliveryRepository;
import com.da2jobu.deliveryservice.domain.deliveryRouteRecord.entity.DeliveryRouteRecord;
import com.da2jobu.deliveryservice.domain.deliveryRouteRecord.repository.DeliveryRouteRecordRepository;
import com.da2jobu.deliveryservice.domain.deliveryRouteRecord.vo.DeliveryRouteStatus;
import common.exception.CustomException;
import common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DeliveryRouteRecordServiceImpl implements DeliveryRouteRecordService {

    private final DeliveryRouteRecordRepository deliveryRouteRecordRepository;
    private final DeliveryRepository deliveryRepository;
    private final DeliveryPermissionChecker permissionChecker;

    @Override
    @Transactional
    public CreateDeliveryRouteRecordsResponseDto createDeliveryRouteRecords(CreateDeliveryRouteRecordsCommand command) {

        List<DeliveryRouteRecord> routeRecords = command.routes()
                .stream()
                .map(route -> DeliveryRouteRecord.builder()
                        .deliveryId(command.deliveryId())
                        .sequence(route.sequence())
                        .originId(route.originId())
                        .originType(route.originType())
                        .destinationId(route.destinationId())
                        .destinationType(route.destinationType())
                        .expectedDistanceKm(route.expectedDistanceKm())
                        .expectedDurationMin(route.expectedDurationMin())
                        .status(DeliveryRouteStatus.HUB_WAITING)
                        .deliveryManagerId(route.deliveryManagerId())
                        .realDistanceKm(null)
                        .realDurationMin(null)
                        .remainDurationMin(route.remainDurationMin())
                        .build())
                .toList();

        List<DeliveryRouteRecord> savedRouteRecords = deliveryRouteRecordRepository.saveAll(routeRecords);

        List<CreateDeliveryRouteRecordsResponseDto.RouteRecordInfo> routeInfos = savedRouteRecords.stream()
                .map(record -> new CreateDeliveryRouteRecordsResponseDto.RouteRecordInfo(
                        record.getDeliveryRouteRecordId(),
                        record.getSequence()
                ))
                .toList();

        return CreateDeliveryRouteRecordsResponseDto.of(command.deliveryId(), routeInfos);
    }

    @Override
    public DeliveryRouteRecordDetailResponseDto getDeliveryRouteRecord(UUID routeRecordId,
                                                                        UUID requesterId, String requesterRole) {
        DeliveryRouteRecord routeRecord = findRouteRecordOrThrow(routeRecordId);
        Delivery delivery = findDeliveryOrThrow(routeRecord.getDeliveryId());

        permissionChecker.checkRouteRecordAccess(routeRecord, delivery, requesterId, requesterRole);

        return DeliveryRouteRecordDetailResponseDto.from(routeRecord);
    }

    @Override
    public DeliveryRouteRecordListResponseDto getDeliveryRouteRecords(UUID deliveryId,
                                                                       UUID requesterId, String requesterRole) {
        Delivery delivery = findDeliveryOrThrow(deliveryId);

        permissionChecker.checkDeliveryRouteListAccess(delivery, requesterId, requesterRole);

        List<DeliveryRouteRecordSummaryResponseDto> routes = deliveryRouteRecordRepository
                .findAllByDeliveryIdAndDeletedAtIsNullOrderBySequenceAsc(deliveryId)
                .stream()
                .map(DeliveryRouteRecordSummaryResponseDto::from)
                .toList();

        return DeliveryRouteRecordListResponseDto.from(routes);
    }

    @Override
    @Transactional
    public UpdateDeliveryRouteStatusResponseDto updateDeliveryRouteStatus(
            UUID routeRecordId,
            UpdateDeliveryRouteStatusCommand command,
            UUID requesterId, String requesterRole
    ) {
        DeliveryRouteRecord routeRecord = findRouteRecordOrThrow(routeRecordId);
        Delivery delivery = findDeliveryOrThrow(routeRecord.getDeliveryId());

        permissionChecker.checkRouteRecordModifyAccess(routeRecord, delivery, requesterId, requesterRole);

        routeRecord.updateStatus(command.status());

        return UpdateDeliveryRouteStatusResponseDto.from(routeRecord);
    }

    @Override
    @Transactional
    public UpdateDeliveryRouteMetricsResponseDto updateDeliveryRouteMetrics(
            UUID routeRecordId,
            UpdateDeliveryRouteMetricsCommand command,
            UUID requesterId, String requesterRole
    ) {
        DeliveryRouteRecord routeRecord = findRouteRecordOrThrow(routeRecordId);
        Delivery delivery = findDeliveryOrThrow(routeRecord.getDeliveryId());

        permissionChecker.checkRouteRecordModifyAccess(routeRecord, delivery, requesterId, requesterRole);

        routeRecord.updateMetrics(command.actualDistanceKm(), command.actualDurationMin());
        routeRecord.updateRemainDurationMin(command.remainDurationMin());

        return UpdateDeliveryRouteMetricsResponseDto.from(routeRecord);
    }

    @Override
    @Transactional
    public DeleteDeliveryRouteRecordResponseDto deleteDeliveryRouteRecord(UUID routeRecordId, String deletedBy,
                                                                           UUID requesterId, String requesterRole) {
        DeliveryRouteRecord routeRecord = findRouteRecordOrThrow(routeRecordId);
        Delivery delivery = findDeliveryOrThrow(routeRecord.getDeliveryId());

        permissionChecker.checkRouteRecordDeleteAccess(routeRecord, delivery, requesterId, requesterRole);

        routeRecord.softDelete(deletedBy);

        return DeleteDeliveryRouteRecordResponseDto.of("배송 경로가 논리 삭제되었습니다.");
    }

    // ── private helpers ────────────────────────────────────────────────────────

    private DeliveryRouteRecord findRouteRecordOrThrow(UUID routeRecordId) {
        return deliveryRouteRecordRepository
                .findByDeliveryRouteRecordIdAndDeletedAtIsNull(routeRecordId)
                .orElseThrow(() -> new CustomException(ErrorCode.DELIVERY_ROUTE_RECORD_NOT_FOUND));
    }

    private Delivery findDeliveryOrThrow(UUID deliveryId) {
        return deliveryRepository.findByDeliveryIdAndDeletedAtIsNull(deliveryId)
                .orElseThrow(() -> new CustomException(ErrorCode.DELIVERY_NOT_FOUND));
    }
}