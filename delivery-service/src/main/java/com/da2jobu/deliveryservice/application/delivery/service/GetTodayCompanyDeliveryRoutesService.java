package com.da2jobu.deliveryservice.application.delivery.service;

import com.da2jobu.deliveryservice.application.delivery.dto.TodayCompanyDeliveryRouteResponseDto;

import java.util.List;
import java.util.UUID;

public interface GetTodayCompanyDeliveryRoutesService {
    List<TodayCompanyDeliveryRouteResponseDto> getTodayCompanyDeliveryRoutes(UUID hubId);
}
