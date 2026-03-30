package com.delivery.hub.interfaces.dto.Request;

import com.delivery.hub.application.dto.CreateHubCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "허브 생성 요청")
public record CreateHubRequest(

        @NotBlank(message = "허브 이름은 필수입니다.")
        @Schema(description = "허브 이름", example = "경기 남부 허브")
        String name,

        @NotBlank(message = "허브 주소는 필수입니다.")
        @Schema(description = "허브 주소", example = "경기도 이천시 ...")
        String address,

        @NotNull(message = "위도는 필수입니다.")
        @Schema(description = "위도", example = "37.123456")
        Double latitude,

        @NotNull(message = "경도는 필수입니다.")
        @Schema(description = "경도", example = "127.123456")
        Double longitude
)
{
    public CreateHubCommand toCommand() {
        return new CreateHubCommand(
                this.name,
                this.address,
                this.latitude,
                this.longitude
        );
    }
}
