package com.da2jobu.deliveryservice.application.delivery.service;

import com.da2jobu.deliveryservice.domain.delivery.entity.Delivery;
import com.da2jobu.deliveryservice.domain.deliveryManager.model.entity.DeliveryManager;
import com.da2jobu.deliveryservice.domain.deliveryManager.model.vo.UserId;
import com.da2jobu.deliveryservice.domain.deliveryManager.repository.DeliveryManagerRepository;
import com.da2jobu.deliveryservice.domain.deliveryRouteRecord.entity.DeliveryRouteRecord;
import com.da2jobu.deliveryservice.domain.deliveryRouteRecord.repository.DeliveryRouteRecordRepository;
import com.da2jobu.deliveryservice.domain.deliveryRouteRecord.vo.RouteLocationType;
import com.da2jobu.deliveryservice.infrastructure.delivery.client.UserServiceClient;
import com.da2jobu.deliveryservice.infrastructure.delivery.dto.UserInfoByIdDto;
import common.exception.CustomException;
import common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * 역할별 배송/경로 소유권 검증을 담당하는 컴포넌트.
 *
 * <pre>
 * MASTER          : 전체 접근 허용
 * HUB_MANAGER     : 담당 허브(originHubId 또는 destinationHubId)와 연관된 배송/경로만 접근 가능
 * DELIVERY_MANAGER: 본인에게 배정된 배송 또는 경로만 접근 가능
 *                   - 업체 배송 담당자: delivery.companyDeliveryManagerId == 본인 managerId
 *                   - 허브 배송 담당자: routeRecord.deliveryManagerId == 본인 managerId
 *                   두 타입 모두 동일하게 처리한다.
 * COMPANY_MANAGER : 발송 업체(supplierCompanyId) 또는 수령 업체(receiverCompanyId)가 본인 업체인 배송/경로만 조회 가능
 * </pre>
 */
@Component
@RequiredArgsConstructor
public class DeliveryPermissionChecker {

    private final UserServiceClient userServiceClient;
    private final DeliveryManagerRepository deliveryManagerRepository;
    private final DeliveryRouteRecordRepository deliveryRouteRecordRepository;

    // ── 배송 단건 접근 ────────────────────────────────────────────────────────

    /**
     * 배송 단건 접근 권한 검증.
     * 권한이 없으면 {@link CustomException}(FORBIDDEN)을 던진다.
     */
    public void checkDeliveryAccess(Delivery delivery, UUID requesterId, String requesterRole) {
        switch (requesterRole) {
            case "MASTER" -> { /* 전체 허용 */ }
            case "HUB_MANAGER" -> checkHubRelatedDelivery(delivery, requesterId);
            case "DELIVERY_MANAGER" -> checkManagerRelatedDelivery(delivery, requesterId);
            case "COMPANY_MANAGER" -> checkCompanyRelatedDelivery(delivery, requesterId);
            default -> throw new CustomException(ErrorCode.FORBIDDEN);
        }
    }

    /**
     * 배송 상태 수정 권한 검증 (COMPANY_MANAGER는 불가).
     */
    public void checkDeliveryStatusUpdateAccess(Delivery delivery, UUID requesterId, String requesterRole) {
        if ("COMPANY_MANAGER".equals(requesterRole)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }
        checkDeliveryAccess(delivery, requesterId, requesterRole);
    }

    /**
     * 배송 삭제 권한 검증 (DELIVERY_MANAGER, COMPANY_MANAGER는 불가).
     */
    public void checkDeliveryDeleteAccess(Delivery delivery, UUID requesterId, String requesterRole) {
        if ("DELIVERY_MANAGER".equals(requesterRole) || "COMPANY_MANAGER".equals(requesterRole)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }
        checkDeliveryAccess(delivery, requesterId, requesterRole);
    }

    // ── 경로 레코드 접근 ─────────────────────────────────────────────────────

    /**
     * 경로 레코드 단건 접근 권한 검증.
     * COMPANY_MANAGER는 배송 엔티티 기준으로 판별하므로 {@code delivery}도 함께 받는다.
     */
    public void checkRouteRecordAccess(DeliveryRouteRecord routeRecord, Delivery delivery,
                                       UUID requesterId, String requesterRole) {
        switch (requesterRole) {
            case "MASTER" -> { /* 전체 허용 */ }
            case "HUB_MANAGER" -> checkHubRelatedRoute(routeRecord, requesterId);
            case "DELIVERY_MANAGER" -> {
                UUID managerId = fetchDeliveryManagerId(requesterId);
                if (!managerId.equals(routeRecord.getDeliveryManagerId())) {
                    throw new CustomException(ErrorCode.FORBIDDEN);
                }
            }
            case "COMPANY_MANAGER" -> checkCompanyRelatedDelivery(delivery, requesterId);
            default -> throw new CustomException(ErrorCode.FORBIDDEN);
        }
    }

    /**
     * 경로 목록({@code GET /deliveries/{deliveryId}/routes}) 접근 권한 검증.
     * 배송 수준 접근 가능 여부로 판단하되,
     * DELIVERY_MANAGER는 허브/업체 담당자 모두 커버한다.
     */
    public void checkDeliveryRouteListAccess(Delivery delivery, UUID requesterId, String requesterRole) {
        switch (requesterRole) {
            case "MASTER" -> { /* 전체 허용 */ }
            case "HUB_MANAGER" -> checkHubRelatedDelivery(delivery, requesterId);
            case "DELIVERY_MANAGER" -> checkManagerRelatedDelivery(delivery, requesterId);
            case "COMPANY_MANAGER" -> checkCompanyRelatedDelivery(delivery, requesterId);
            default -> throw new CustomException(ErrorCode.FORBIDDEN);
        }
    }

    /**
     * 경로 상태/실측 수정 권한 검증 (COMPANY_MANAGER는 불가).
     */
    public void checkRouteRecordModifyAccess(DeliveryRouteRecord routeRecord, Delivery delivery,
                                             UUID requesterId, String requesterRole) {
        if ("COMPANY_MANAGER".equals(requesterRole)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }
        checkRouteRecordAccess(routeRecord, delivery, requesterId, requesterRole);
    }

    /**
     * 경로 삭제 권한 검증 (DELIVERY_MANAGER, COMPANY_MANAGER는 불가).
     */
    public void checkRouteRecordDeleteAccess(DeliveryRouteRecord routeRecord, Delivery delivery,
                                             UUID requesterId, String requesterRole) {
        if ("DELIVERY_MANAGER".equals(requesterRole) || "COMPANY_MANAGER".equals(requesterRole)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }
        checkRouteRecordAccess(routeRecord, delivery, requesterId, requesterRole);
    }

    // ── public helpers (목록 쿼리 필터링 등 서비스 계층에서 사용) ──────────────

    public UUID fetchHubId(UUID userId) {
        UserInfoByIdDto userInfo = userServiceClient.getUserByUserId(userId).getData();
        if (userInfo == null || userInfo.hubId() == null) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }
        return userInfo.hubId();
    }

    public UUID fetchCompanyId(UUID userId) {
        UserInfoByIdDto userInfo = userServiceClient.getUserByUserId(userId).getData();
        if (userInfo == null || userInfo.companyId() == null) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }
        return userInfo.companyId();
    }

    public UUID fetchDeliveryManagerId(UUID userId) {
        DeliveryManager manager = deliveryManagerRepository.findByUserId(UserId.of(userId))
                .orElseThrow(() -> new CustomException(ErrorCode.FORBIDDEN));
        return manager.getDeliveryManagerId().getDeliveryManagerId();
    }

    // ── private 판별 메서드 ──────────────────────────────────────────────────

    /** HUB_MANAGER: originHubId 또는 destinationHubId가 담당 허브인지 확인 */
    private void checkHubRelatedDelivery(Delivery delivery, UUID requesterId) {
        UUID hubId = fetchHubId(requesterId);
        if (!delivery.getOriginHubId().equals(hubId) && !delivery.getDestinationHubId().equals(hubId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }
    }

    /**
     * DELIVERY_MANAGER: 배송 수준 접근 판별.
     * <ul>
     *   <li>업체 배송 담당자: {@code delivery.companyDeliveryManagerId == managerId}</li>
     *   <li>허브 배송 담당자: 해당 배송의 경로 중 하나라도 {@code routeRecord.deliveryManagerId == managerId}</li>
     * </ul>
     */
    private void checkManagerRelatedDelivery(Delivery delivery, UUID requesterId) {
        UUID managerId = fetchDeliveryManagerId(requesterId);
        boolean isCompanyManager = managerId.equals(delivery.getCompanyDeliveryManagerId());
        boolean isRouteManager = deliveryRouteRecordRepository
                .existsByDeliveryIdAndDeliveryManagerIdAndDeletedAtIsNull(delivery.getDeliveryId(), managerId);
        if (!isCompanyManager && !isRouteManager) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }
    }

    /**
     * COMPANY_MANAGER: 발송 업체(supplierCompanyId) 또는 수령 업체(receiverCompanyId)가 본인 업체인지 확인.
     */
    private void checkCompanyRelatedDelivery(Delivery delivery, UUID requesterId) {
        UUID companyId = fetchCompanyId(requesterId);
        boolean isSupplier = companyId.equals(delivery.getSupplierCompanyId());
        boolean isReceiver = companyId.equals(delivery.getReceiverCompanyId());
        if (!isSupplier && !isReceiver) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }
    }

    /** HUB_MANAGER: 경로의 origin 또는 destination이 담당 허브인지 확인 */
    private void checkHubRelatedRoute(DeliveryRouteRecord routeRecord, UUID requesterId) {
        UUID hubId = fetchHubId(requesterId);
        boolean originMatch = RouteLocationType.HUB == routeRecord.getOriginType()
                && routeRecord.getOriginId().equals(hubId);
        boolean destMatch = RouteLocationType.HUB == routeRecord.getDestinationType()
                && routeRecord.getDestinationId().equals(hubId);
        if (!originMatch && !destMatch) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }
    }
}