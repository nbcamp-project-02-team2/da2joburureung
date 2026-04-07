package com.da2jobu.deliveryservice.application.delivery.service;

import com.da2jobu.deliveryservice.application.delivery.command.CreateDeliveryCommand;
import com.da2jobu.deliveryservice.application.delivery.command.UpdateDeliveryStatusCommand;
import com.da2jobu.deliveryservice.application.delivery.dto.CreateDeliveryResponseDto;
import com.da2jobu.deliveryservice.application.delivery.dto.DeliveryDetailResponseDto;
import com.da2jobu.deliveryservice.application.delivery.dto.DeliveryListResponseDto;
import com.da2jobu.deliveryservice.application.delivery.dto.DeliverySummaryResponseDto;
import com.da2jobu.deliveryservice.domain.delivery.entity.Delivery;
import com.da2jobu.deliveryservice.domain.delivery.repository.DeliveryRepository;
import com.da2jobu.deliveryservice.domain.delivery.vo.DeliveryStatus;
import common.exception.CustomException;
import common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DeliveryServiceImpl implements DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final DeliveryPermissionChecker permissionChecker;

    @Override
    @Transactional
    public CreateDeliveryResponseDto createDelivery(CreateDeliveryCommand command) {

        Delivery delivery = Delivery.builder()
                .orderId(command.orderId())
                .status(command.status() != null ? command.status() : DeliveryStatus.HUB_WAITING)
                .originHubId(command.originHubId())
                .destinationHubId(command.destinationHubId())
                .deliveryAddress(command.deliveryAddress())
                .receiverName(command.receiverName())
                .receiverSlackId(command.receiverSlackId())
                .supplierCompanyId(command.supplierCompanyId())
                .receiverCompanyId(command.receiverCompanyId())
                .companyDeliveryManagerId(command.companyDeliveryManagerId())
                .requestNote(command.requestNote())
                .expectedDurationTotalMin(command.expectedDurationTotalMin())
                .desiredDeliveryAt(command.desiredDeliveryAt())
                .startedAt(command.startedAt())
                .completedAt(command.completedAt())
                .build();

        Delivery savedDelivery = deliveryRepository.save(delivery);

        return CreateDeliveryResponseDto.from(savedDelivery);
    }

    @Override
    public DeliveryDetailResponseDto getDelivery(UUID deliveryId, UUID requesterId, String requesterRole) {
        Delivery delivery = deliveryRepository.findByDeliveryIdAndDeletedAtIsNull(deliveryId)
                .orElseThrow(() -> new CustomException(ErrorCode.DELIVERY_NOT_FOUND));

        permissionChecker.checkDeliveryAccess(delivery, requesterId, requesterRole);

        return DeliveryDetailResponseDto.from(delivery);
    }

    @Override
    public DeliveryListResponseDto getDeliveries(
            UUID orderId,
            DeliveryStatus status,
            UUID originHubId,
            UUID destinationHubId,
            int page,
            int size,
            UUID requesterId,
            String requesterRole
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Delivery> deliveryPage = fetchDeliveriesByRole(
                orderId, status, originHubId, destinationHubId, pageable, requesterId, requesterRole
        );

        List<DeliverySummaryResponseDto> content = deliveryPage.getContent()
                .stream()
                .map(DeliverySummaryResponseDto::from)
                .toList();

        Page<DeliverySummaryResponseDto> responsePage = new PageImpl<>(
                content, pageable, deliveryPage.getTotalElements()
        );

        return DeliveryListResponseDto.from(responsePage);
    }

    @Override
    @Transactional
    public DeliveryDetailResponseDto updateDeliveryStatus(UUID deliveryId, UpdateDeliveryStatusCommand command,
                                                          UUID requesterId, String requesterRole) {
        Delivery delivery = deliveryRepository.findByDeliveryIdAndDeletedAtIsNull(deliveryId)
                .orElseThrow(() -> new CustomException(ErrorCode.DELIVERY_NOT_FOUND));

        permissionChecker.checkDeliveryStatusUpdateAccess(delivery, requesterId, requesterRole);

        delivery.updateStatus(command.status());

        return DeliveryDetailResponseDto.from(delivery);
    }

    @Override
    @Transactional
    public void deleteDelivery(UUID deliveryId, String deletedBy, UUID requesterId, String requesterRole) {
        Delivery delivery = deliveryRepository.findByDeliveryIdAndDeletedAtIsNull(deliveryId)
                .orElseThrow(() -> new CustomException(ErrorCode.DELIVERY_NOT_FOUND));

        permissionChecker.checkDeliveryDeleteAccess(delivery, requesterId, requesterRole);

        if (delivery.isDeleted()) {
            throw new CustomException(ErrorCode.DELIVERY_ALREADY_DELETED);
        }

        delivery.softDelete(deletedBy);
    }

    // ── private helpers ────────────────────────────────────────────────────────

    /**
     * 역할에 따라 적절한 배송 목록 쿼리를 선택한다.
     * MASTER는 기존 필터 파라미터를 그대로 사용하고,
     * 나머지 역할은 소유권 기반 필터를 우선 적용한다.
     */
    private Page<Delivery> fetchDeliveriesByRole(
            UUID orderId, DeliveryStatus status, UUID originHubId, UUID destinationHubId,
            Pageable pageable, UUID requesterId, String requesterRole
    ) {
        return switch (requesterRole) {
            case "MASTER" -> fetchDeliveriesForMaster(orderId, status, originHubId, destinationHubId, pageable);
            case "HUB_MANAGER" -> {
                UUID hubId = permissionChecker.fetchHubId(requesterId);
                yield deliveryRepository.findByHubRelatedAndDeletedAtIsNull(hubId, pageable);
            }
            case "DELIVERY_MANAGER" -> {
                // 허브 배송 담당자(route 배정)와 업체 배송 담당자(delivery 직접 배정) 모두 커버
                UUID managerId = permissionChecker.fetchDeliveryManagerId(requesterId);
                yield deliveryRepository.findByManagerRelatedAndDeletedAtIsNull(managerId, pageable);
            }
            case "COMPANY_MANAGER" -> {
                // 발송 업체 또는 수령 업체가 본인 업체인 배송 모두 조회
                UUID companyId = permissionChecker.fetchCompanyId(requesterId);
                yield deliveryRepository.findByCompanyRelatedAndDeletedAtIsNull(companyId, pageable);
            }
            default -> throw new CustomException(ErrorCode.FORBIDDEN);
        };
    }

    private Page<Delivery> fetchDeliveriesForMaster(
            UUID orderId, DeliveryStatus status, UUID originHubId, UUID destinationHubId, Pageable pageable
    ) {
        if (orderId != null) {
            return deliveryRepository.findByOrderIdAndDeletedAtIsNull(orderId, pageable);
        } else if (status != null) {
            return deliveryRepository.findByStatusAndDeletedAtIsNull(status, pageable);
        } else if (originHubId != null) {
            return deliveryRepository.findByOriginHubIdAndDeletedAtIsNull(originHubId, pageable);
        } else if (destinationHubId != null) {
            return deliveryRepository.findByDestinationHubIdAndDeletedAtIsNull(destinationHubId, pageable);
        } else {
            return deliveryRepository.findAllByDeletedAtIsNull(pageable);
        }
    }
}