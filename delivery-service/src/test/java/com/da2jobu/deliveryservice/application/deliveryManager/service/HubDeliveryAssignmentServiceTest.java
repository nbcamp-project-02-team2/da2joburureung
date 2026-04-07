package com.da2jobu.deliveryservice.application.deliveryManager.service;

import com.da2jobu.deliveryservice.domain.delivery.entity.Delivery;
import com.da2jobu.deliveryservice.domain.delivery.repository.DeliveryRepository;
import com.da2jobu.deliveryservice.domain.deliveryManager.model.entity.DeliveryAssignment;
import com.da2jobu.deliveryservice.domain.deliveryManager.model.entity.DeliveryManager;
import com.da2jobu.deliveryservice.domain.deliveryManager.model.vo.*;
import com.da2jobu.deliveryservice.domain.deliveryManager.repository.DeliveryAssignmentRepository;
import com.da2jobu.deliveryservice.domain.deliveryManager.repository.DeliveryManagerRepository;
import com.da2jobu.deliveryservice.domain.deliveryManager.service.DeliveryAssignmentDomainService;
import com.da2jobu.deliveryservice.domain.deliveryRouteRecord.entity.DeliveryRouteRecord;
import com.da2jobu.deliveryservice.domain.deliveryRouteRecord.repository.DeliveryRouteRecordRepository;
import common.exception.CustomException;
import common.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 허브 배송담당자 배정 서비스 테스트
 * - 업체 배송담당자 배정(VRPTW 배치)과 달리, 단건 배송 발생 시 즉시 배정
 * - 배정 기준: 대기시간 긴 담당자 우선, 동일하면 seq 오름차순 (라운드로빈)
 * - 시간 윈도우 제약 없음 (발제문 기준, 이동 시공간 제약 무시)
 */
class HubDeliveryAssignmentServiceTest {

    @Mock private DeliveryManagerRepository deliveryManagerRepository;
    @Mock private DeliveryAssignmentRepository deliveryAssignmentRepository;
    @Mock private DeliveryAssignmentDomainService deliveryAssignmentDomainService;
    @Mock private DeliveryRepository deliveryRepository;
    @Mock private DeliveryRouteRecordRepository deliveryRouteRecordRepository;

    @InjectMocks
    private HubDeliveryAssignmentService hubDeliveryAssignmentService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    private DeliveryManager hubManager(int seq) {
        return DeliveryManager.create(
                UserId.of(UUID.randomUUID()),
                null,
                DeliveryManagerType.HUB_DELIVERY,
                seq);
    }

    // ── 정상 배정 ─────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("정상 배정 케이스")
    class SuccessCase {

        @Test
        @DisplayName("가용 허브 담당자 중 우선순위 1위에게 즉시 배정")
        void assign_to_best_candidate() {
            UUID deliveryId = UUID.randomUUID();
            UUID routeRecordId = UUID.randomUUID();
            UUID startHubId = UUID.randomUUID();
            DeliveryManager bestManager = hubManager(1);

            when(deliveryManagerRepository.findHubDeliveryManagersWithNoAssignment())
                    .thenReturn(List.of(bestManager));
            when(deliveryAssignmentDomainService.selectBestCandidate(anyList()))
                    .thenReturn(bestManager);
            when(deliveryRepository.findByDeliveryIdAndDeletedAtIsNull(deliveryId))
                    .thenReturn(Optional.of(mock(Delivery.class)));
            when(deliveryRouteRecordRepository.findByDeliveryRouteRecordIdAndDeletedAtIsNull(routeRecordId))
                    .thenReturn(Optional.of(mock(DeliveryRouteRecord.class)));
            when(deliveryAssignmentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            hubDeliveryAssignmentService.assignHubDelivery(
                    DeliveryId.of(deliveryId), DeliveryRouteRecordId.of(routeRecordId), startHubId);

            verify(deliveryAssignmentRepository, times(1)).save(any(DeliveryAssignment.class));
        }

        @Test
        @DisplayName("저장된 assignment에 담당자 ID, 배송 ID, 시작 허브 ID, 상태(ASSIGNED)가 올바르게 설정된다")
        void assignment_fields_are_correct() {
            UUID deliveryId = UUID.randomUUID();
            UUID routeRecordId = UUID.randomUUID();
            UUID startHubId = UUID.randomUUID();
            DeliveryManager mgr = hubManager(1);

            when(deliveryManagerRepository.findHubDeliveryManagersWithNoAssignment())
                    .thenReturn(List.of(mgr));
            when(deliveryAssignmentDomainService.selectBestCandidate(anyList()))
                    .thenReturn(mgr);
            when(deliveryRepository.findByDeliveryIdAndDeletedAtIsNull(deliveryId))
                    .thenReturn(Optional.of(mock(Delivery.class)));
            when(deliveryRouteRecordRepository.findByDeliveryRouteRecordIdAndDeletedAtIsNull(routeRecordId))
                    .thenReturn(Optional.of(mock(DeliveryRouteRecord.class)));

            ArgumentCaptor<DeliveryAssignment> captor = ArgumentCaptor.forClass(DeliveryAssignment.class);
            when(deliveryAssignmentRepository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

            hubDeliveryAssignmentService.assignHubDelivery(
                    DeliveryId.of(deliveryId), DeliveryRouteRecordId.of(routeRecordId), startHubId);

            DeliveryAssignment saved = captor.getValue();
            assertThat(saved.getDeliveryManagerId()).isEqualTo(mgr.getDeliveryManagerId());
            assertThat(saved.getDeliveryId()).isEqualTo(DeliveryId.of(deliveryId));
            assertThat(saved.getDeliveryRouteRecordId()).isEqualTo(DeliveryRouteRecordId.of(routeRecordId));
            assertThat(saved.getHubId()).isEqualTo(HubId.of(startHubId));
            assertThat(saved.getStatus()).isEqualTo(DeliveryAssignmentStatus.ASSIGNED);
        }

        @Test
        @DisplayName("배정 후 delivery와 deliveryRouteRecord에 담당자 ID가 업데이트된다")
        void delivery_and_route_record_manager_id_updated() {
            UUID deliveryId = UUID.randomUUID();
            UUID routeRecordId = UUID.randomUUID();
            UUID startHubId = UUID.randomUUID();
            DeliveryManager mgr = hubManager(1);
            Delivery delivery = mock(Delivery.class);
            DeliveryRouteRecord routeRecord = mock(DeliveryRouteRecord.class);

            when(deliveryManagerRepository.findHubDeliveryManagersWithNoAssignment())
                    .thenReturn(List.of(mgr));
            when(deliveryAssignmentDomainService.selectBestCandidate(anyList()))
                    .thenReturn(mgr);
            when(deliveryRepository.findByDeliveryIdAndDeletedAtIsNull(deliveryId))
                    .thenReturn(Optional.of(delivery));
            when(deliveryRouteRecordRepository.findByDeliveryRouteRecordIdAndDeletedAtIsNull(routeRecordId))
                    .thenReturn(Optional.of(routeRecord));
            when(deliveryAssignmentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            hubDeliveryAssignmentService.assignHubDelivery(
                    DeliveryId.of(deliveryId), DeliveryRouteRecordId.of(routeRecordId), startHubId);

            UUID expectedManagerId = mgr.getDeliveryManagerId().getDeliveryManagerId();
            verify(delivery, times(1)).updateManagerId(expectedManagerId);
            verify(routeRecord, times(1)).updateManagerId(expectedManagerId);
        }

        @Test
        @DisplayName("배송 완료 시 assignment 상태가 COMPLETED로 변경된다")
        void complete_hub_delivery_changes_status() {
            UUID assignmentId = UUID.randomUUID();
            DeliveryAssignment assignment = DeliveryAssignment.create(
                    DeliveryManagerId.of(UUID.randomUUID()),
                    DeliveryId.of(UUID.randomUUID()),
                    DeliveryRouteRecordId.of(UUID.randomUUID()),
                    HubId.of(UUID.randomUUID()));

            when(deliveryAssignmentRepository.findById(DeliveryAssignmentId.of(assignmentId)))
                    .thenReturn(Optional.of(assignment));

            hubDeliveryAssignmentService.completeDeliveryAssignment(assignmentId);

            assertThat(assignment.getStatus()).isEqualTo(DeliveryAssignmentStatus.COMPLETED);
        }
    }

    // ── 예외 케이스 ───────────────────────────────────────────────────────────

    @Nested
    @DisplayName("예외 케이스")
    class ExceptionCase {

        @Test
        @DisplayName("가용 허브 담당자가 없으면 DELIVERY_MANAGER_NOT_FOUND 예외")
        void no_available_hub_managers_throws() {
            UUID deliveryId = UUID.randomUUID();
            UUID routeRecordId = UUID.randomUUID();
            UUID startHubId = UUID.randomUUID();

            when(deliveryManagerRepository.findHubDeliveryManagersWithNoAssignment())
                    .thenReturn(List.of());

            assertThatThrownBy(() ->
                    hubDeliveryAssignmentService.assignHubDelivery(
                            DeliveryId.of(deliveryId), DeliveryRouteRecordId.of(routeRecordId), startHubId))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(ErrorCode.DELIVERY_MANAGER_NOT_FOUND.getMessage());

            verify(deliveryAssignmentDomainService, never()).selectBestCandidate(any());
            verify(deliveryAssignmentRepository, never()).save(any());
        }

        @Test
        @DisplayName("배정 시 delivery가 존재하지 않으면 DELIVERY_NOT_FOUND 예외")
        void delivery_not_found_throws() {
            UUID deliveryId = UUID.randomUUID();
            UUID routeRecordId = UUID.randomUUID();
            UUID startHubId = UUID.randomUUID();
            DeliveryManager mgr = hubManager(1);

            when(deliveryManagerRepository.findHubDeliveryManagersWithNoAssignment())
                    .thenReturn(List.of(mgr));
            when(deliveryAssignmentDomainService.selectBestCandidate(anyList()))
                    .thenReturn(mgr);
            when(deliveryRepository.findByDeliveryIdAndDeletedAtIsNull(deliveryId))
                    .thenReturn(Optional.empty());
            when(deliveryAssignmentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            assertThatThrownBy(() ->
                    hubDeliveryAssignmentService.assignHubDelivery(
                            DeliveryId.of(deliveryId), DeliveryRouteRecordId.of(routeRecordId), startHubId))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(ErrorCode.DELIVERY_NOT_FOUND.getMessage());
        }

        @Test
        @DisplayName("배정 시 deliveryRouteRecord가 존재하지 않으면 DELIVERY_ROUTE_RECORD_NOT_FOUND 예외")
        void route_record_not_found_throws() {
            UUID deliveryId = UUID.randomUUID();
            UUID routeRecordId = UUID.randomUUID();
            UUID startHubId = UUID.randomUUID();
            DeliveryManager mgr = hubManager(1);

            when(deliveryManagerRepository.findHubDeliveryManagersWithNoAssignment())
                    .thenReturn(List.of(mgr));
            when(deliveryAssignmentDomainService.selectBestCandidate(anyList()))
                    .thenReturn(mgr);
            when(deliveryRepository.findByDeliveryIdAndDeletedAtIsNull(deliveryId))
                    .thenReturn(Optional.of(mock(Delivery.class)));
            when(deliveryRouteRecordRepository.findByDeliveryRouteRecordIdAndDeletedAtIsNull(routeRecordId))
                    .thenReturn(Optional.empty());
            when(deliveryAssignmentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            assertThatThrownBy(() ->
                    hubDeliveryAssignmentService.assignHubDelivery(
                            DeliveryId.of(deliveryId), DeliveryRouteRecordId.of(routeRecordId), startHubId))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(ErrorCode.DELIVERY_ROUTE_RECORD_NOT_FOUND.getMessage());
        }

        @Test
        @DisplayName("완료 처리 시 assignment가 존재하지 않으면 DELIVERY_ASSIGNMENT_NOT_FOUND 예외")
        void complete_assignment_not_found_throws() {
            UUID assignmentId = UUID.randomUUID();

            when(deliveryAssignmentRepository.findById(DeliveryAssignmentId.of(assignmentId)))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    hubDeliveryAssignmentService.completeDeliveryAssignment(assignmentId))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(ErrorCode.DELIVERY_ASSIGNMENT_NOT_FOUND.getMessage());
        }
    }
}