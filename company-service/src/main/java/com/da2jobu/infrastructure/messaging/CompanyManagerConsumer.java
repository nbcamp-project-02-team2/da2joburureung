package com.da2jobu.infrastructure.messaging;

import com.da2jobu.application.CompanyService;
import com.da2jobu.infrastructure.messaging.event.CompanyManagerAssignedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CompanyManagerConsumer {

    private final CompanyService companyService;

    @KafkaListener(topics = "company.manager.assigned", groupId = "company-service")
    public void handleCompanyManagerAssigned(CompanyManagerAssignedEvent event) {
        log.info("업체 담당자 배정 이벤트 수신. companyId={}, userId={}", event.companyId(), event.userId());
        companyService.assignManager(event.companyId(), event.userId());
    }
}