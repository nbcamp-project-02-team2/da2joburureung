package com.da2jobu.infrastructure.messaging.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record CompanyDeletedEvent(
        UUID companyId,
        String deletedBy,
        LocalDateTime deletedAt
) {}