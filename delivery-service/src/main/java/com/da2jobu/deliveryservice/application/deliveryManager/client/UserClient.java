package com.da2jobu.deliveryservice.application.deliveryManager.client;

import java.util.UUID;

public interface UserClient {
    void validateUserAndRole(UUID userId);
}