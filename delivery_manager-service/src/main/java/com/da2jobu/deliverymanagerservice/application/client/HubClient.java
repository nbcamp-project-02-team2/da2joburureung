package com.da2jobu.deliverymanagerservice.application.client;

import java.util.UUID;

public interface HubClient {
    boolean validateHubExists(UUID hubId);
    void validateHubManager(UUID hubId, UUID requesterId);
}
