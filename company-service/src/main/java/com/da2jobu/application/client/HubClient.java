package com.da2jobu.application.client;

import java.util.UUID;

public interface HubClient {
    boolean validateHubExists(UUID hubId);
}
