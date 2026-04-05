package com.da2jobu.deliveryservice.infrastructure.delivery.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record CompanyInfoDto(
        UUID companyId,
        UUID hubId,
        String address,
        BigDecimal latitude,
        BigDecimal longitude
) {
}
