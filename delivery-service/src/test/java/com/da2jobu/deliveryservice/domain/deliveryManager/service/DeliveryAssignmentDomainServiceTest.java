package com.da2jobu.deliveryservice.domain.deliveryManager.service;

import com.da2jobu.deliveryservice.domain.deliveryManager.model.entity.DeliveryManager;
import com.da2jobu.deliveryservice.domain.deliveryManager.model.vo.*;
import com.da2jobu.deliveryservice.domain.deliveryManager.repository.DeliveryAssignmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

/**
 * 배송담당자 우선순위 정렬 도메인 서비스 테스트
 * - 업체/허브 담당자 공통으로 사용
 * - 정렬 기준: 대기시간(idle duration) 내림차순 → seq 오름차순
 * - 배정 이력 없으면 createdAt 기준으로 대기시간 계산
 */
class DeliveryAssignmentDomainServiceTest {

    @Mock
    private DeliveryAssignmentRepository deliveryAssignmentRepository;

    private DeliveryAssignmentDomainService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new DeliveryAssignmentDomainService(deliveryAssignmentRepository);
    }

    private DeliveryManager companyManager(UUID hubId, int seq) {
        return DeliveryManager.create(
                UserId.of(UUID.randomUUID()), HubId.of(hubId),
                DeliveryManagerType.COMPANY_DELIVERY, seq);
    }

    private DeliveryManager hubManager(int seq) {
        return DeliveryManager.create(
                UserId.of(UUID.randomUUID()), null,
                DeliveryManagerType.HUB_DELIVERY, seq);
    }

    // ── rankManagersByPriority ────────────────────────────────────────────────

    @Nested
    @DisplayName("rankManagersByPriority - 우선순위 정렬")
    class RankManagersByPriority {

        @Test
        @DisplayName("대기시간이 긴 담당자가 앞에 정렬된다")
        void longer_idle_first() {
            UUID hubId = UUID.randomUUID();
            DeliveryManager mgr1 = companyManager(hubId, 1); // 대기 1시간
            DeliveryManager mgr2 = companyManager(hubId, 2); // 대기 3시간

            when(deliveryAssignmentRepository.findIdleDurationsByManagerIds(anyList()))
                    .thenReturn(List.of(
                            new ManagerIdleDuration(mgr1.getDeliveryManagerId(), Duration.ofHours(1)),
                            new ManagerIdleDuration(mgr2.getDeliveryManagerId(), Duration.ofHours(3))));

            List<DeliveryManager> result = service.rankManagersByPriority(List.of(mgr1, mgr2));

            assertThat(result.get(0).getDeliveryManagerId()).isEqualTo(mgr2.getDeliveryManagerId());
            assertThat(result.get(1).getDeliveryManagerId()).isEqualTo(mgr1.getDeliveryManagerId());
        }

        @Test
        @DisplayName("대기시간이 동일하면 seq 오름차순으로 정렬된다")
        void same_idle_then_seq_asc() {
            UUID hubId = UUID.randomUUID();
            DeliveryManager mgr1 = companyManager(hubId, 1);
            DeliveryManager mgr2 = companyManager(hubId, 2);
            DeliveryManager mgr3 = companyManager(hubId, 3);

            when(deliveryAssignmentRepository.findIdleDurationsByManagerIds(anyList()))
                    .thenReturn(List.of(
                            new ManagerIdleDuration(mgr1.getDeliveryManagerId(), Duration.ofHours(2)),
                            new ManagerIdleDuration(mgr2.getDeliveryManagerId(), Duration.ofHours(2)),
                            new ManagerIdleDuration(mgr3.getDeliveryManagerId(), Duration.ofHours(2))));

            List<DeliveryManager> result = service.rankManagersByPriority(List.of(mgr3, mgr1, mgr2));

            assertThat(result.get(0).getSeq()).isEqualTo(1);
            assertThat(result.get(1).getSeq()).isEqualTo(2);
            assertThat(result.get(2).getSeq()).isEqualTo(3);
        }

        @Test
        @DisplayName("허브 배송담당자도 동일한 우선순위 기준으로 정렬된다")
        void hub_managers_ranked_by_same_rule() {
            DeliveryManager hub1 = hubManager(1); // 대기 2시간
            DeliveryManager hub2 = hubManager(2); // 대기 4시간

            when(deliveryAssignmentRepository.findIdleDurationsByManagerIds(anyList()))
                    .thenReturn(List.of(
                            new ManagerIdleDuration(hub1.getDeliveryManagerId(), Duration.ofHours(2)),
                            new ManagerIdleDuration(hub2.getDeliveryManagerId(), Duration.ofHours(4))));

            List<DeliveryManager> result = service.rankManagersByPriority(List.of(hub1, hub2));

            assertThat(result.get(0).getDeliveryManagerId()).isEqualTo(hub2.getDeliveryManagerId());
        }
    }

    // ── selectBestCandidate ───────────────────────────────────────────────────

    @Nested
    @DisplayName("selectBestCandidate - 최우선 담당자 선정")
    class SelectBestCandidate {

        @Test
        @DisplayName("담당자 1명이면 해당 담당자가 선정된다")
        void single_manager_selected() {
            DeliveryManager mgr = hubManager(1);

            when(deliveryAssignmentRepository.findIdleDurationsByManagerIds(anyList()))
                    .thenReturn(List.of(
                            new ManagerIdleDuration(mgr.getDeliveryManagerId(), Duration.ofHours(1))));

            DeliveryManager best = service.selectBestCandidate(List.of(mgr));

            assertThat(best.getDeliveryManagerId()).isEqualTo(mgr.getDeliveryManagerId());
        }

        @Test
        @DisplayName("여러 담당자 중 대기시간이 가장 긴 담당자가 선정된다")
        void longest_idle_manager_selected() {
            DeliveryManager mgr1 = hubManager(1); // 대기 1시간
            DeliveryManager mgr2 = hubManager(2); // 대기 5시간
            DeliveryManager mgr3 = hubManager(3); // 대기 3시간

            when(deliveryAssignmentRepository.findIdleDurationsByManagerIds(anyList()))
                    .thenReturn(List.of(
                            new ManagerIdleDuration(mgr1.getDeliveryManagerId(), Duration.ofHours(1)),
                            new ManagerIdleDuration(mgr2.getDeliveryManagerId(), Duration.ofHours(5)),
                            new ManagerIdleDuration(mgr3.getDeliveryManagerId(), Duration.ofHours(3))));

            DeliveryManager best = service.selectBestCandidate(List.of(mgr1, mgr2, mgr3));

            assertThat(best.getDeliveryManagerId()).isEqualTo(mgr2.getDeliveryManagerId());
        }

        @Test
        @DisplayName("대기시간 동일하면 seq가 가장 낮은 담당자가 선정된다")
        void same_idle_selects_lowest_seq() {
            UUID hubId = UUID.randomUUID();
            DeliveryManager mgr2 = companyManager(hubId, 2);
            DeliveryManager mgr5 = companyManager(hubId, 5);

            when(deliveryAssignmentRepository.findIdleDurationsByManagerIds(anyList()))
                    .thenReturn(List.of(
                            new ManagerIdleDuration(mgr2.getDeliveryManagerId(), Duration.ofHours(2)),
                            new ManagerIdleDuration(mgr5.getDeliveryManagerId(), Duration.ofHours(2))));

            DeliveryManager best = service.selectBestCandidate(List.of(mgr5, mgr2));

            assertThat(best.getSeq()).isEqualTo(2);
        }
    }
}