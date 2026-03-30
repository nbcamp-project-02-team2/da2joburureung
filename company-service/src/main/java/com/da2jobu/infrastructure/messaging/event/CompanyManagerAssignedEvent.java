package com.da2jobu.infrastructure.messaging.event;

import java.util.UUID;

public record CompanyManagerAssignedEvent(
        UUID userId,
        UUID companyId
) {}