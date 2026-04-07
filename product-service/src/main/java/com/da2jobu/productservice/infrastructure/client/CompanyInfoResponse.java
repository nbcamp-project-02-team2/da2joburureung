package com.da2jobu.productservice.infrastructure.client;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "업체 서비스 조회 응답")
public class CompanyInfoResponse {

    @Schema(description = "업체 ID", example = "33333333-3333-3333-3333-333333333333")
    private UUID companyId;

    @Schema(description = "소속 허브 ID", example = "22222222-2222-2222-2222-222222222222")
    private UUID hubId;
}
