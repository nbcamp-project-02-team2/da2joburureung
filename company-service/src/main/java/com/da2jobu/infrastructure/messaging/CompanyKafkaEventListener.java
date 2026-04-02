package com.da2jobu.infrastructure.messaging;

import com.da2jobu.infrastructure.messaging.event.CompanyDeletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class CompanyKafkaEventListener {

    private static final String COMPANY_DELETED_TOPIC = "company.deleted.v1";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCompanyDeleted(CompanyDeletedEvent event) {
        kafkaTemplate.send(COMPANY_DELETED_TOPIC, event.companyId().toString(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("이벤트 발행 실패: companyId={}, cause={}", event.companyId(), ex.getMessage());
                    } else {
                        log.info("이벤트 발행 완료: companyId={}, topic={}", event.companyId(), COMPANY_DELETED_TOPIC);
                    }
                });
    }
}
