package com.da2jobu.application.service;

import java.util.UUID;

public interface HubClient {
    void validateExists(UUID hubId);
}
