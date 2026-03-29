package com.da2jobu.application.service;

import java.util.UUID;

public interface UserClient {
    void validateUserExistsAndRole(UUID managerId);
}
