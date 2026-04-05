package com.da2jobu.productservice.infrastructure.client;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class UserInfoResponse {
    private UUID hubId;
    private UUID companyId;
}
