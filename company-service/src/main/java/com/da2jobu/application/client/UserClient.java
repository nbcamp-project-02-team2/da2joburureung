package com.da2jobu.application.client;

import java.util.UUID;

public interface UserClient {
    UserInfo getUserInfo(UUID userId);

    record UserInfo(UUID hubId, UUID companyId) {}
}