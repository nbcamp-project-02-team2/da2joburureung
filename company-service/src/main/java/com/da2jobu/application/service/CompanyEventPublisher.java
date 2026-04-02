package com.da2jobu.application.service;

import java.time.LocalDateTime;
import java.util.UUID;

public interface CompanyEventPublisher {
    void publishCompanyDeleted(UUID companyId, String deletedBy, LocalDateTime deletedAt);
}