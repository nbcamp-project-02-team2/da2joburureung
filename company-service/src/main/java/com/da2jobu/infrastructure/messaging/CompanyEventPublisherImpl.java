package com.da2jobu.infrastructure.messaging;

import com.da2jobu.application.messaging.CompanyEventPublisher;
import com.da2jobu.infrastructure.messaging.outbox.OutboxEvent;
import com.da2jobu.infrastructure.messaging.outbox.OutboxEventRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class CompanyEventPublisherImpl implements CompanyEventPublisher {

    private static final String COMPANY_DELETED_TOPIC = "company.deleted.v1";

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    @Override
    public void publishCompanyDeleted(UUID companyId, String deletedBy, LocalDateTime deletedAt) {
        try {
            String payload = objectMapper.writeValueAsString(Map.of(
                    "companyId", companyId.toString(),
                    "deletedBy", deletedBy,
                    "deletedAt", deletedAt.toString()
            ));

            OutboxEvent outboxEvent = OutboxEvent.create(
                    "Company",
                    companyId,
                    "CompanyDeleted",
                    COMPANY_DELETED_TOPIC,
                    payload
            );

            outboxEventRepository.save(outboxEvent);
            log.debug("아웃박스 이벤트 저장 완료: companyId={}", companyId);

        } catch (JsonProcessingException e) {
            log.error("아웃박스 이벤트 직렬화 실패: companyId={}", companyId, e);
            throw new RuntimeException("이벤트 페이로드 직렬화 실패", e);
        }
    }
}