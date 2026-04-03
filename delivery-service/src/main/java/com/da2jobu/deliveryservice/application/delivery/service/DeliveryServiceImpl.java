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

    @Override
    @Transactional
    public CreateDeliveryResponseDto createDelivery(CreateDeliveryCommand command) {

        Delivery delivery = Delivery.builder()
                .orderId(command.orderId())
                .status(command.status() != null ? command.status() : DeliveryStatus.HUB_WAITING)   // status를 기본 값으로 HUB_WAITING 으로 설정
                .originHubId(command.originHubId())
                .destinationHubId(command.destinationHubId())
                .deliveryAddress(command.deliveryAddress())
                .receiverName(command.receiverName())
                .receiverSlackId(command.receiverSlackId())
                .companyDeliveryManagerId(command.companyDeliveryManagerId())
                .requestNote(command.requestNote())
                .expectedDurationTotalMin(command.expectedDurationTotalMin())
                .startedAt(command.startedAt())
                .completedAt(command.completedAt())
                .build();

        Delivery savedDelivery = deliveryRepository.save(delivery);

        return CreateDeliveryResponseDto.from(savedDelivery);
    }

    @Override
    public DeliveryDetailResponseDto getDelivery(UUID deliveryId) {
        Delivery delivery = deliveryRepository.findByDeliveryIdAndDeletedAtIsNull(deliveryId)
                .orElseThrow(() -> new CustomException(ErrorCode.DELIVERY_NOT_FOUND));

        return DeliveryDetailResponseDto.from(delivery);
    }

    @Override
    public DeliveryListResponseDto getDeliveries(
            UUID orderId,
            DeliveryStatus status,
            UUID originHubId,
            UUID destinationHubId,
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Delivery> deliveryPage;

        if (orderId != null) {
            deliveryPage = deliveryRepository.findByOrderIdAndDeletedAtIsNull(orderId, pageable);
        } else if (status != null) {
            deliveryPage = deliveryRepository.findByStatusAndDeletedAtIsNull(status, pageable);
        } else if (originHubId != null) {
            deliveryPage = deliveryRepository.findByOriginHubIdAndDeletedAtIsNull(originHubId, pageable);
        } else if (destinationHubId != null) {
            deliveryPage = deliveryRepository.findByDestinationHubIdAndDeletedAtIsNull(destinationHubId, pageable);
        } else {
            deliveryPage = deliveryRepository.findAllByDeletedAtIsNull(pageable);
        }

        List<DeliverySummaryResponseDto> content = deliveryPage.getContent()
                .stream()
                .map(DeliverySummaryResponseDto::from)
                .toList();

        Page<DeliverySummaryResponseDto> responsePage = new PageImpl<>(
                content,
                pageable,
                deliveryPage.getTotalElements()
        );

        return DeliveryListResponseDto.from(responsePage);
    }

    @Override
    @Transactional
    public DeliveryDetailResponseDto updateDeliveryStatus(UUID deliveryId, UpdateDeliveryStatusCommand command) {
        Delivery delivery = deliveryRepository.findByDeliveryIdAndDeletedAtIsNull(deliveryId)
                .orElseThrow(() -> new CustomException(ErrorCode.DELIVERY_NOT_FOUND));

        delivery.updateStatus(command.status());

        return DeliveryDetailResponseDto.from(delivery);
    }

    @Override
    @Transactional
    public void deleteDelivery(UUID deliveryId, String deletedBy) {
        Delivery delivery = deliveryRepository.findByDeliveryIdAndDeletedAtIsNull(deliveryId)
                .orElseThrow(() -> new CustomException(ErrorCode.DELIVERY_NOT_FOUND));

        if (delivery.isDeleted()) {
            throw new CustomException(ErrorCode.DELIVERY_ALREADY_DELETED);
        }

        delivery.softDelete(deletedBy);
    }
}
