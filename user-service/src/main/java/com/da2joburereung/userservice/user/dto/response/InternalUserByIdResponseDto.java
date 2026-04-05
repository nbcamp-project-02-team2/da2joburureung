package com.da2joburereung.userservice.user.dto.response;

import com.da2joburereung.userservice.user.domain.UserRole;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class InternalUserByIdResponseDto {
    private UUID userId;
    private UserRole userRole;
    private UUID hubId;
    private UUID companyId;
}
