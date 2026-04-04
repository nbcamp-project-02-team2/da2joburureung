package com.da2jobu.productservice.infrastructure.client;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class CompanyInfoResponse {
    private UUID companyId;
    private UUID hubId;
}
