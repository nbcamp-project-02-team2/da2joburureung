package com.da2jobu.deliverymanagerservice.application.client;

import java.util.UUID;

public interface UserClient {
    void validateUserAndRole(UUID userId);
}