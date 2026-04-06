package com.da2jobu.infrastructure.messaging.outbox;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OutboxEventSchedulerTest {

    @InjectMocks
    private OutboxEventScheduler scheduler;

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @SuppressWarnings("unchecked")
    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    private OutboxEvent event1;
    private OutboxEvent event2;

    @BeforeEach
    void setUp() {
        event1 = OutboxEvent.create("Company", UUID.randomUUID(), "CompanyDeleted", "company.deleted.v1", "{\"companyId\":\"uuid\"}");
        event2 = OutboxEvent.create("Company", UUID.randomUUID(), "CompanyDeleted", "company.deleted.v1", "{\"companyId\":\"uuid2\"}");
    }

    @Test
    @DisplayName("PENDING 이벤트 없음 - 조기 반환")
    void noPendingEvents() {
        given(outboxEventRepository.findByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING))
                .willReturn(List.of());

        scheduler.publishPendingEvents();

        then(kafkaTemplate).should(never()).send(anyString(), anyString(), anyString());
    }

    @Nested
    @DisplayName("Kafka 발행 성공")
    class KafkaSuccess {

        @Test
        @DisplayName("markPublished + save 호출")
        void publishedAndSaved() {
            given(outboxEventRepository.findByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING))
                    .willReturn(List.of(event1));
            given(kafkaTemplate.send(anyString(), anyString(), anyString()))
                    .willReturn(CompletableFuture.completedFuture(mock(SendResult.class)));

            scheduler.publishPendingEvents();

            then(outboxEventRepository).should().save(event1);
            assertThat(event1.getStatus()).isEqualTo(OutboxStatus.PUBLISHED);
            assertThat(event1.getPublishedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("Kafka 발행 실패")
    class KafkaFailure {

        @Test
        @DisplayName("save 호출 없이 PENDING 유지 - 다음 주기에 재시도")
        void staysPendingForNextCycle() {
            given(outboxEventRepository.findByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING))
                    .willReturn(List.of(event1));
            given(kafkaTemplate.send(anyString(), anyString(), anyString()))
                    .willReturn(CompletableFuture.failedFuture(new RuntimeException("Kafka 오류")));

            scheduler.publishPendingEvents();

            then(outboxEventRepository).should(never()).save(event1);
            assertThat(event1.getStatus()).isEqualTo(OutboxStatus.PENDING);
        }

        @Test
        @DisplayName("첫 번째 이벤트 실패해도 두 번째 이벤트 처리")
        void continuesNextEventOnFailure() {
            given(outboxEventRepository.findByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING))
                    .willReturn(List.of(event1, event2));
            given(kafkaTemplate.send(anyString(), anyString(), anyString()))
                    .willReturn(CompletableFuture.failedFuture(new RuntimeException("Kafka 오류")))
                    .willReturn(CompletableFuture.completedFuture(mock(SendResult.class)));

            scheduler.publishPendingEvents();

            then(outboxEventRepository).should(never()).save(event1);
            then(outboxEventRepository).should().save(event2);
            assertThat(event1.getStatus()).isEqualTo(OutboxStatus.PENDING);
            assertThat(event2.getStatus()).isEqualTo(OutboxStatus.PUBLISHED);
        }
    }
}
