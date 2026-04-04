package com.da2jobu.deliveryservice.application.deliveryManager.client;

import java.util.UUID;

public interface HubClient {
    boolean validateHubExists(UUID hubId);
    void validateHubManager(UUID hubId, UUID requesterId);
}
