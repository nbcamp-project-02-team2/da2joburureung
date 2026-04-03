package com.da2jobu.application.service;

import java.util.UUID;

public interface HubClient {
    boolean validateHubExists(UUID hubId);
}
