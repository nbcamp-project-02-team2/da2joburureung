package com.da2jobu.deliveryservice.presentation;

import com.da2jobu.deliveryservice.application.dto.CreateDeliveryResponseDto;
import com.da2jobu.deliveryservice.application.dto.DeliveryDetailResponseDto;
import com.da2jobu.deliveryservice.application.dto.DeliveryListResponseDto;
import com.da2jobu.deliveryservice.application.service.DeliveryService;
import com.da2jobu.deliveryservice.domain.vo.DeliveryStatus;
import com.da2jobu.deliveryservice.presentation.request.CreateDeliveryRequest;
import com.da2jobu.deliveryservice.presentation.request.UpdateDeliveryStatusRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class DeliveryController {

    private final DeliveryService deliveryService;

    @PostMapping("/internal/deliveries")
    public CreateDeliveryResponseDto createDelivery(@Valid @RequestBody CreateDeliveryRequest request) {
        return deliveryService.createDelivery(request.toCommand());
    }

    @GetMapping("/deliveries/{deliveryId}")
    public DeliveryDetailResponseDto getDelivery(@PathVariable UUID deliveryId) {
        return deliveryService.getDelivery(deliveryId);
    }

    @GetMapping("/deliveries")
    public DeliveryListResponseDto getDeliveries(
            @RequestParam(required = false) UUID orderId,
            @RequestParam(required = false) DeliveryStatus status,
            @RequestParam(required = false) UUID originHubId,
            @RequestParam(required = false) UUID destinationHubId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return deliveryService.getDeliveries(
                orderId,
                status,
                originHubId,
                destinationHubId,
                page,
                size
        );
    }

    @PutMapping("/deliveries/{deliveryId}/status")
    public DeliveryDetailResponseDto updateDeliveryStatus(@PathVariable UUID deliveryId, @Valid @RequestBody UpdateDeliveryStatusRequest request) {
        return deliveryService.updateDeliveryStatus(deliveryId, request.toCommand());
    }

    @DeleteMapping("/deliveries/{deliveryId}")
    public void deleteDelivery(@PathVariable UUID deliveryId, @RequestParam String deletedBy) {
        deliveryService.deleteDelivery(deliveryId, deletedBy);
    }
}
