package com.da2jobu.infrastructure.messaging;

import com.da2jobu.application.service.CompanyEventPublisher;
import com.da2jobu.infrastructure.messaging.event.CompanyDeletedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CompanyEventPublisherImpl implements CompanyEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void publishCompanyDeleted(UUID companyId, String deletedBy, LocalDateTime deletedAt) {
        applicationEventPublisher.publishEvent(new CompanyDeletedEvent(companyId, deletedBy, deletedAt));
    }
}
