package com.da2jobu.domain.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "업체 타입")
public enum CompanyType {

    @Schema(description = "공급 업체")
    PRODUCER,

    @Schema(description = "수령 업체")
    RECEIVER
}