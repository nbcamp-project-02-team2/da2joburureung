package com.da2jobu.deliveryservice.application.deliveryManager.service;

import com.da2jobu.deliveryservice.application.delivery.dto.TodayCompanyDeliveryRouteResponseDto;
import com.da2jobu.deliveryservice.application.delivery.service.GetTodayCompanyDeliveryRoutesService;
import com.da2jobu.deliveryservice.application.deliveryManager.dto.CompanyDeliveryPoint;
import com.da2jobu.deliveryservice.application.deliveryManager.dto.VehicleRoute;
import com.da2jobu.deliveryservice.application.deliveryManager.dto.VrptwResult;
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
import com.da2jobu.deliveryservice.domain.deliveryRouteRecord.vo.RouteLocationType;
import com.da2jobu.deliveryservice.infrastructure.delivery.client.CompanyServiceClient;
import com.da2jobu.deliveryservice.infrastructure.delivery.dto.CompanyInfoDto;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

/**
 * 업체 배송담당자 배정 서비스 테스트
 * - 매일 6시 스케줄러 실행 → 허브별 당일 배송 건 조회 → VRPTW 경로 최적화 → 담당자 배정
 * - desiredDeliveryAt은 VRPTW 시간 윈도우 상한(deadline)으로 사용
 */
class CompanyDeliveryAssignmentServiceTest {

    @Mock private DeliveryManagerRepository deliveryManagerRepository;
    @Mock private RouteOptimizationService routeOptimizationService;
    @Mock private GetTodayCompanyDeliveryRoutesService getTodayCompanyDeliveryRoutesService;
    @Mock private DeliveryAssignmentRepository deliveryAssignmentRepository;
    @Mock private DeliveryAssignmentDomainService deliveryAssignmentDomainService;
    @Mock private CompanyServiceClient companyServiceClient;
    @Mock private DeliveryRepository deliveryRepository;
    @Mock private DeliveryRouteRecordRepository deliveryRouteRecordRepository;

    @InjectMocks
    private CompanyDeliveryAssignmentService companyDeliveryAssignmentService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // ── 헬퍼 ─────────────────────────────────────────────────────────────────

    private DeliveryManager manager(UUID hubId, int seq) {
        return DeliveryManager.create(
                UserId.of(UUID.randomUUID()), HubId.of(hubId),
                DeliveryManagerType.COMPANY_DELIVERY, seq);
    }

    private TodayCompanyDeliveryRouteResponseDto routeDto(UUID hubId, UUID deliveryId,
                                                           UUID routeId, UUID destId,
                                                           LocalDateTime desiredAt) {
        return new TodayCompanyDeliveryRouteResponseDto(
                routeId, deliveryId, 1,
                hubId, RouteLocationType.HUB,
                destId, RouteLocationType.COMPANY,
                BigDecimal.valueOf(10.0), 60,
                null, null, 60,
                null, null, desiredAt);
    }

    private CompanyDeliveryPoint point(UUID deliveryId, UUID routeId, UUID destId,
                                       LocalDateTime desiredAt) {
        return CompanyDeliveryPoint.of(
                deliveryId, routeId, destId,
                BigDecimal.valueOf(10.0), 60, desiredAt,
                BigDecimal.valueOf(37.5), BigDecimal.valueOf(127.0));
    }

    private CompanyInfoDto companyInfo(UUID destId, UUID hubId) {
        return new CompanyInfoDto(destId, hubId, "서울시 강남구",
                BigDecimal.valueOf(37.5), BigDecimal.valueOf(127.0));
    }

    /** 정상 케이스 공통 mock 세팅 */
    private void mockCommonSuccess(UUID hubId, UUID deliveryId, UUID routeId, UUID destId,
                                   LocalDateTime desiredAt, DeliveryManager mgr) {
        when(getTodayCompanyDeliveryRoutesService.getTodayCompanyDeliveryRoutes(hubId))
                .thenReturn(List.of(routeDto(hubId, deliveryId, routeId, destId, desiredAt)));
        when(deliveryManagerRepository.findAvailableCompanyManagersByHub(HubId.of(hubId)))
                .thenReturn(List.of(mgr));
        when(companyServiceClient.getCompanies(anyList()))
                .thenReturn(List.of(companyInfo(destId, hubId)));

        VehicleRoute route = VehicleRoute.of(0, List.of(point(deliveryId, routeId, destId, desiredAt)), 10.0);
        when(routeOptimizationService.solve(any()))
                .thenReturn(VrptwResult.of(List.of(route), 10.0, true));
        when(deliveryAssignmentDomainService.rankManagersByPriority(anyList()))
                .thenReturn(List.of(mgr));
        when(deliveryRepository.findByDeliveryIdAndDeletedAtIsNull(deliveryId))
                .thenReturn(Optional.of(mock(Delivery.class)));
        when(deliveryRouteRecordRepository.findByDeliveryRouteRecordIdAndDeletedAtIsNull(routeId))
                .thenReturn(Optional.of(mock(DeliveryRouteRecord.class)));
        when(deliveryAssignmentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    // ── 정상 배정 ─────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("정상 배정 케이스")
    class SuccessCase {

        @Test
        @DisplayName("배송 1건, 담당자 1명 - 수령 희망일 이내 배정 성공")
        void single_delivery_before_desired_time() {
            UUID hubId = UUID.randomUUID();
            UUID deliveryId = UUID.randomUUID();
            UUID routeId = UUID.randomUUID();
            UUID destId = UUID.randomUUID();
            // 수령 희망일 14시 - 6시 출발 기준 여유 있음
            LocalDateTime desiredAt = LocalDateTime.of(2026, 4, 6, 14, 0);
            DeliveryManager mgr = manager(hubId, 1);

            mockCommonSuccess(hubId, deliveryId, routeId, destId, desiredAt, mgr);

            companyDeliveryAssignmentService.assignDailyCompanyDeliveries(hubId);

            verify(deliveryAssignmentRepository, times(1)).save(any(DeliveryAssignment.class));
            verify(mock(Delivery.class), never()).updateManagerId(any()); // 실제 호출은 내부에서 됨
        }

        @Test
        @DisplayName("배송 3건, 담당자 2명 - 경로 분배 후 총 3건 배정")
        void three_deliveries_two_managers() {
            UUID hubId = UUID.randomUUID();
            DeliveryManager mgr1 = manager(hubId, 1);
            DeliveryManager mgr2 = manager(hubId, 2);

            UUID d1 = UUID.randomUUID(), d2 = UUID.randomUUID(), d3 = UUID.randomUUID();
            UUID r1 = UUID.randomUUID(), r2 = UUID.randomUUID(), r3 = UUID.randomUUID();
            UUID dest1 = UUID.randomUUID(), dest2 = UUID.randomUUID(), dest3 = UUID.randomUUID();
            LocalDateTime desiredAt = LocalDateTime.of(2026, 4, 6, 18, 0);

            when(getTodayCompanyDeliveryRoutesService.getTodayCompanyDeliveryRoutes(hubId))
                    .thenReturn(List.of(
                            routeDto(hubId, d1, r1, dest1, desiredAt),
                            routeDto(hubId, d2, r2, dest2, desiredAt),
                            routeDto(hubId, d3, r3, dest3, desiredAt)));
            when(deliveryManagerRepository.findAvailableCompanyManagersByHub(HubId.of(hubId)))
                    .thenReturn(List.of(mgr1, mgr2));
            when(companyServiceClient.getCompanies(anyList()))
                    .thenReturn(List.of(companyInfo(dest1, hubId), companyInfo(dest2, hubId), companyInfo(dest3, hubId)));

            // 담당자 2명 → 경로 2개 (mgr1: 배송 2건, mgr2: 배송 1건)
            VehicleRoute route1 = VehicleRoute.of(0, List.of(
                    point(d1, r1, dest1, desiredAt), point(d2, r2, dest2, desiredAt)), 20.0);
            VehicleRoute route2 = VehicleRoute.of(1, List.of(
                    point(d3, r3, dest3, desiredAt)), 10.0);
            when(routeOptimizationService.solve(any()))
                    .thenReturn(VrptwResult.of(List.of(route1, route2), 30.0, true));
            when(deliveryAssignmentDomainService.rankManagersByPriority(anyList()))
                    .thenReturn(List.of(mgr1, mgr2));
            when(deliveryRepository.findByDeliveryIdAndDeletedAtIsNull(any()))
                    .thenReturn(Optional.of(mock(Delivery.class)));
            when(deliveryRouteRecordRepository.findByDeliveryRouteRecordIdAndDeletedAtIsNull(any()))
                    .thenReturn(Optional.of(mock(DeliveryRouteRecord.class)));
            when(deliveryAssignmentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            companyDeliveryAssignmentService.assignDailyCompanyDeliveries(hubId);

            verify(deliveryAssignmentRepository, times(3)).save(any(DeliveryAssignment.class));
        }

        @Test
        @DisplayName("저장된 DeliveryAssignment에 담당자 ID, 배송 ID, 허브 ID, 상태(ASSIGNED)가 올바르게 설정된다")
        void assignment_fields_are_correct() {
            UUID hubId = UUID.randomUUID();
            UUID deliveryId = UUID.randomUUID();
            UUID routeId = UUID.randomUUID();
            UUID destId = UUID.randomUUID();
            LocalDateTime desiredAt = LocalDateTime.of(2026, 4, 6, 14, 0);
            DeliveryManager mgr = manager(hubId, 1);

            mockCommonSuccess(hubId, deliveryId, routeId, destId, desiredAt, mgr);

            ArgumentCaptor<DeliveryAssignment> captor = ArgumentCaptor.forClass(DeliveryAssignment.class);
            when(deliveryAssignmentRepository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

            companyDeliveryAssignmentService.assignDailyCompanyDeliveries(hubId);

            DeliveryAssignment saved = captor.getValue();
            assertThat(saved.getDeliveryManagerId()).isEqualTo(mgr.getDeliveryManagerId());
            assertThat(saved.getDeliveryId()).isEqualTo(DeliveryId.of(deliveryId));
            assertThat(saved.getDeliveryRouteRecordId()).isEqualTo(DeliveryRouteRecordId.of(routeId));
            assertThat(saved.getHubId()).isEqualTo(HubId.of(hubId));
            assertThat(saved.getStatus()).isEqualTo(DeliveryAssignmentStatus.ASSIGNED);
        }

        @Test
        @DisplayName("배정 후 delivery와 deliveryRouteRecord에 담당자 ID가 업데이트된다")
        void delivery_and_route_record_manager_id_updated() {
            UUID hubId = UUID.randomUUID();
            UUID deliveryId = UUID.randomUUID();
            UUID routeId = UUID.randomUUID();
            UUID destId = UUID.randomUUID();
            LocalDateTime desiredAt = LocalDateTime.of(2026, 4, 6, 14, 0);
            DeliveryManager mgr = manager(hubId, 1);

            Delivery delivery = mock(Delivery.class);
            DeliveryRouteRecord routeRecord = mock(DeliveryRouteRecord.class);

            when(getTodayCompanyDeliveryRoutesService.getTodayCompanyDeliveryRoutes(hubId))
                    .thenReturn(List.of(routeDto(hubId, deliveryId, routeId, destId, desiredAt)));
            when(deliveryManagerRepository.findAvailableCompanyManagersByHub(HubId.of(hubId)))
                    .thenReturn(List.of(mgr));
            when(companyServiceClient.getCompanies(anyList()))
                    .thenReturn(List.of(companyInfo(destId, hubId)));
            VehicleRoute route = VehicleRoute.of(0, List.of(point(deliveryId, routeId, destId, desiredAt)), 10.0);
            when(routeOptimizationService.solve(any()))
                    .thenReturn(VrptwResult.of(List.of(route), 10.0, true));
            when(deliveryAssignmentDomainService.rankManagersByPriority(anyList()))
                    .thenReturn(List.of(mgr));
            when(deliveryRepository.findByDeliveryIdAndDeletedAtIsNull(deliveryId))
                    .thenReturn(Optional.of(delivery));
            when(deliveryRouteRecordRepository.findByDeliveryRouteRecordIdAndDeletedAtIsNull(routeId))
                    .thenReturn(Optional.of(routeRecord));
            when(deliveryAssignmentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            companyDeliveryAssignmentService.assignDailyCompanyDeliveries(hubId);

            verify(delivery, times(1)).updateManagerId(mgr.getDeliveryManagerId().getDeliveryManagerId());
            verify(routeRecord, times(1)).updateManagerId(mgr.getDeliveryManagerId().getDeliveryManagerId());
        }

        @Test
        @DisplayName("수령 희망일시가 null(시간 제약 없음)인 배송도 정상 배정된다")
        void delivery_with_null_desired_time_is_assigned() {
            UUID hubId = UUID.randomUUID();
            UUID deliveryId = UUID.randomUUID();
            UUID routeId = UUID.randomUUID();
            UUID destId = UUID.randomUUID();
            // desiredDeliveryAt = null → VRPTW 시간 윈도우 없이 처리
            DeliveryManager mgr = manager(hubId, 1);

            mockCommonSuccess(hubId, deliveryId, routeId, destId, null, mgr);

            companyDeliveryAssignmentService.assignDailyCompanyDeliveries(hubId);

            verify(deliveryAssignmentRepository, times(1)).save(any(DeliveryAssignment.class));
        }
    }

    // ── 수령 희망일 초과 (VRPTW 시간 윈도우 처리) ─────────────────────────────

    @Nested
    @DisplayName("수령 희망일 초과 케이스 (VRPTW 시간 윈도우)")
    class DesiredTimeWindowCase {

        @Test
        @DisplayName("희망 시간 내 배송 불가 → 시간 윈도우 완화 후 배정 성공")
        void infeasible_first_then_relaxed_feasible() {
            UUID hubId = UUID.randomUUID();
            UUID deliveryId = UUID.randomUUID();
            UUID routeId = UUID.randomUUID();
            UUID destId = UUID.randomUUID();
            // 수령 희망일 7시 → 6시 출발 후 1시간 내 도달해야 하나 거리가 멀어 불가
            LocalDateTime tightDeadline = LocalDateTime.of(2026, 4, 6, 7, 0);
            DeliveryManager mgr = manager(hubId, 1);

            when(getTodayCompanyDeliveryRoutesService.getTodayCompanyDeliveryRoutes(hubId))
                    .thenReturn(List.of(routeDto(hubId, deliveryId, routeId, destId, tightDeadline)));
            when(deliveryManagerRepository.findAvailableCompanyManagersByHub(HubId.of(hubId)))
                    .thenReturn(List.of(mgr));
            when(companyServiceClient.getCompanies(anyList()))
                    .thenReturn(List.of(companyInfo(destId, hubId)));

            VehicleRoute relaxedRoute = VehicleRoute.of(0, List.of(point(deliveryId, routeId, destId, null)), 10.0);
            when(routeOptimizationService.solve(any()))
                    .thenReturn(VrptwResult.of(List.of(), 0.0, false))     // 1차: 시간 윈도우 위반
                    .thenReturn(VrptwResult.of(List.of(relaxedRoute), 10.0, true)); // 2차: 완화 후 성공
            when(deliveryAssignmentDomainService.rankManagersByPriority(anyList()))
                    .thenReturn(List.of(mgr));
            when(deliveryRepository.findByDeliveryIdAndDeletedAtIsNull(deliveryId))
                    .thenReturn(Optional.of(mock(Delivery.class)));
            when(deliveryRouteRecordRepository.findByDeliveryRouteRecordIdAndDeletedAtIsNull(routeId))
                    .thenReturn(Optional.of(mock(DeliveryRouteRecord.class)));
            when(deliveryAssignmentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            companyDeliveryAssignmentService.assignDailyCompanyDeliveries(hubId);

            // 1차(시간 윈도우 적용) + 2차(완화) 총 2번 호출
            verify(routeOptimizationService, times(2)).solve(any());
            verify(deliveryAssignmentRepository, times(1)).save(any(DeliveryAssignment.class));
        }

        @Test
        @DisplayName("시간 윈도우 완화 후에도 경로 불가 → ROUTE_OPTIMIZATION_FAILED 예외")
        void infeasible_even_after_relax_throws_exception() {
            UUID hubId = UUID.randomUUID();
            UUID deliveryId = UUID.randomUUID();
            UUID routeId = UUID.randomUUID();
            UUID destId = UUID.randomUUID();
            LocalDateTime tightDeadline = LocalDateTime.of(2026, 4, 6, 7, 0);
            DeliveryManager mgr = manager(hubId, 1);

            when(getTodayCompanyDeliveryRoutesService.getTodayCompanyDeliveryRoutes(hubId))
                    .thenReturn(List.of(routeDto(hubId, deliveryId, routeId, destId, tightDeadline)));
            when(deliveryManagerRepository.findAvailableCompanyManagersByHub(HubId.of(hubId)))
                    .thenReturn(List.of(mgr));
            when(companyServiceClient.getCompanies(anyList()))
                    .thenReturn(List.of(companyInfo(destId, hubId)));
            when(routeOptimizationService.solve(any()))
                    .thenReturn(VrptwResult.of(List.of(), 0.0, false)); // 1차, 2차 모두 실패

            assertThatThrownBy(() ->
                    companyDeliveryAssignmentService.assignDailyCompanyDeliveries(hubId))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(ErrorCode.ROUTE_OPTIMIZATION_FAILED.getMessage());

            verify(routeOptimizationService, times(2)).solve(any());
            verify(deliveryAssignmentRepository, never()).save(any());
        }
    }

    // ── 예외 케이스 ───────────────────────────────────────────────────────────

    @Nested
    @DisplayName("예외 케이스")
    class ExceptionCase {

        @Test
        @DisplayName("당일 배송 건 없으면 배정 로직 실행하지 않고 종료")
        void no_deliveries_today_skips_all() {
            UUID hubId = UUID.randomUUID();
            when(getTodayCompanyDeliveryRoutesService.getTodayCompanyDeliveryRoutes(hubId))
                    .thenReturn(List.of());

            companyDeliveryAssignmentService.assignDailyCompanyDeliveries(hubId);

            verify(deliveryManagerRepository, never()).findAvailableCompanyManagersByHub(any());
            verify(routeOptimizationService, never()).solve(any());
            verify(deliveryAssignmentRepository, never()).save(any());
        }

        @Test
        @DisplayName("가용 업체 배송담당자 없으면 NO_AVAILABLE_COMPANY_DELIVERY_MANAGER 예외")
        void no_available_managers_throws() {
            UUID hubId = UUID.randomUUID();
            UUID deliveryId = UUID.randomUUID();
            UUID routeId = UUID.randomUUID();
            UUID destId = UUID.randomUUID();

            when(getTodayCompanyDeliveryRoutesService.getTodayCompanyDeliveryRoutes(hubId))
                    .thenReturn(List.of(routeDto(hubId, deliveryId, routeId, destId,
                            LocalDateTime.of(2026, 4, 6, 14, 0))));
            when(deliveryManagerRepository.findAvailableCompanyManagersByHub(HubId.of(hubId)))
                    .thenReturn(List.of());

            assertThatThrownBy(() ->
                    companyDeliveryAssignmentService.assignDailyCompanyDeliveries(hubId))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(ErrorCode.NO_AVAILABLE_COMPANY_DELIVERY_MANAGER.getMessage());

            verify(routeOptimizationService, never()).solve(any());
            verify(deliveryAssignmentRepository, never()).save(any());
        }

        @Test
        @DisplayName("배정 시 delivery가 존재하지 않으면 DELIVERY_NOT_FOUND 예외")
        void delivery_not_found_throws() {
            UUID hubId = UUID.randomUUID();
            UUID deliveryId = UUID.randomUUID();
            UUID routeId = UUID.randomUUID();
            UUID destId = UUID.randomUUID();
            LocalDateTime desiredAt = LocalDateTime.of(2026, 4, 6, 14, 0);
            DeliveryManager mgr = manager(hubId, 1);

            when(getTodayCompanyDeliveryRoutesService.getTodayCompanyDeliveryRoutes(hubId))
                    .thenReturn(List.of(routeDto(hubId, deliveryId, routeId, destId, desiredAt)));
            when(deliveryManagerRepository.findAvailableCompanyManagersByHub(HubId.of(hubId)))
                    .thenReturn(List.of(mgr));
            when(companyServiceClient.getCompanies(anyList()))
                    .thenReturn(List.of(companyInfo(destId, hubId)));
            VehicleRoute route = VehicleRoute.of(0, List.of(point(deliveryId, routeId, destId, desiredAt)), 10.0);
            when(routeOptimizationService.solve(any()))
                    .thenReturn(VrptwResult.of(List.of(route), 10.0, true));
            when(deliveryAssignmentDomainService.rankManagersByPriority(anyList()))
                    .thenReturn(List.of(mgr));
            when(deliveryRepository.findByDeliveryIdAndDeletedAtIsNull(deliveryId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    companyDeliveryAssignmentService.assignDailyCompanyDeliveries(hubId))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(ErrorCode.DELIVERY_NOT_FOUND.getMessage());
        }

        @Test
        @DisplayName("배정 시 deliveryRouteRecord가 존재하지 않으면 DELIVERY_ROUTE_RECORD_NOT_FOUND 예외")
        void route_record_not_found_throws() {
            UUID hubId = UUID.randomUUID();
            UUID deliveryId = UUID.randomUUID();
            UUID routeId = UUID.randomUUID();
            UUID destId = UUID.randomUUID();
            LocalDateTime desiredAt = LocalDateTime.of(2026, 4, 6, 14, 0);
            DeliveryManager mgr = manager(hubId, 1);

            when(getTodayCompanyDeliveryRoutesService.getTodayCompanyDeliveryRoutes(hubId))
                    .thenReturn(List.of(routeDto(hubId, deliveryId, routeId, destId, desiredAt)));
            when(deliveryManagerRepository.findAvailableCompanyManagersByHub(HubId.of(hubId)))
                    .thenReturn(List.of(mgr));
            when(companyServiceClient.getCompanies(anyList()))
                    .thenReturn(List.of(companyInfo(destId, hubId)));
            VehicleRoute route = VehicleRoute.of(0, List.of(point(deliveryId, routeId, destId, desiredAt)), 10.0);
            when(routeOptimizationService.solve(any()))
                    .thenReturn(VrptwResult.of(List.of(route), 10.0, true));
            when(deliveryAssignmentDomainService.rankManagersByPriority(anyList()))
                    .thenReturn(List.of(mgr));
            when(deliveryRepository.findByDeliveryIdAndDeletedAtIsNull(deliveryId))
                    .thenReturn(Optional.of(mock(Delivery.class)));
            when(deliveryRouteRecordRepository.findByDeliveryRouteRecordIdAndDeletedAtIsNull(routeId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    companyDeliveryAssignmentService.assignDailyCompanyDeliveries(hubId))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(ErrorCode.DELIVERY_ROUTE_RECORD_NOT_FOUND.getMessage());
        }
    }
}