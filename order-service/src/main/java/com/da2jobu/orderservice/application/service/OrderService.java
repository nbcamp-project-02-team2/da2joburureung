package com.da2jobu.orderservice.application.service;

import com.da2jobu.orderservice.domain.model.Order;
import com.da2jobu.orderservice.domain.model.OrderStatus;
import com.da2jobu.orderservice.domain.repository.OrderRepository;
import com.da2jobu.orderservice.infrastructure.client.CompanyClient;
import com.da2jobu.orderservice.infrastructure.client.CompanyInfoResponse;
import com.da2jobu.orderservice.infrastructure.client.ProductClient;
import com.da2jobu.orderservice.infrastructure.client.ProductInfoResponse;
import com.da2jobu.orderservice.infrastructure.client.UserClient;
import com.da2jobu.orderservice.infrastructure.event.producer.OrderAcceptedEvent;
import com.da2jobu.orderservice.infrastructure.event.producer.OrderCancelledEvent;
import com.da2jobu.orderservice.infrastructure.event.producer.OrderEventProducer;
import com.da2jobu.orderservice.interfaces.dto.request.OrderCreateRequest;
import com.da2jobu.orderservice.interfaces.dto.request.OrderUpdateRequest;
import com.da2jobu.orderservice.interfaces.dto.response.OrderResponse;
import common.exception.CustomException;
import common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductClient productClient;
    private final CompanyClient companyClient;
    private final UserClient userClient;
    private final OrderEventProducer orderEventProducer;

    /**
     * 1. 주문 생성.
     * - MASTER, HUB_MANAGER: 무조건 가능
     * - COMPANY_MANAGER: 본인 업체가 공급 업체인 경우만 가능
     * - DELIVERY_MANAGER: 생성 불가
     */
    @Transactional
    public OrderResponse createOrder(OrderCreateRequest request,
                                     String userId, String username, String role) {
        if ("DELIVERY_MANAGER".equals(role)) {
            throw new CustomException(ErrorCode.ORDER_CREATE_FORBIDDEN);
        }

        if ("COMPANY_MANAGER".equals(role)) {
            UUID userCompanyId = getUserCompanyId(userId);
            if (!userCompanyId.equals(request.getSupplierId())) {
                throw new CustomException(ErrorCode.ORDER_CREATE_FORBIDDEN);
            }
        }

        // 상품 조회 (재고 + 단가 확인)
        ProductInfoResponse product = productClient.getProduct(request.getProductId()).getData();
        if (product.getStockQuantity() < request.getQuantity()) {
            throw new CustomException(ErrorCode.INSUFFICIENT_STOCK);
        }

        // 공급 업체 조회 (허브 ID 확보)
        CompanyInfoResponse supplier = companyClient.getCompany(request.getSupplierId()).getData();

        Order order = Order.builder()
                .supplierId(request.getSupplierId())
                .receiverId(request.getReceiverId())
                .productId(request.getProductId())
                .quantity(request.getQuantity())
                .unitPrice(product.getPrice())
                .hubId(supplier.getHubId())
                .requirements(request.getRequirements())
                .desiredDeliveryDate(request.getDesiredDeliveryDate())
                .status(OrderStatus.PENDING)
                .build();

        orderRepository.save(order);
        order.accept();

        // DB 커밋 후에 외부 시스템(Feign, Kafka) 호출 — 롤백 시 side effect 방지
        UUID productId = request.getProductId();
        int quantity = request.getQuantity();
        OrderAcceptedEvent event = new OrderAcceptedEvent(
                order.getId(),
                order.getSupplierId(),
                order.getReceiverId(),
                order.getRequirements(),
                username,
                order.getDesiredDeliveryDate()
        );
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                productClient.reduceStock(productId, quantity);
                orderEventProducer.publishOrderAccepted(event);
            }
        });

        return OrderResponse.from(order);
    }

    /**
     * 2. 주문 수정.
     * - MASTER, HUB_MANAGER: 무조건 가능
     * - COMPANY_MANAGER: 본인 업체의 PENDING 주문만 수정 가능
     * - DELIVERY_MANAGER: 수정 불가
     */
    @Transactional
    public OrderResponse updateOrder(UUID orderId, OrderUpdateRequest request,
                                     String userId, String username, String role) {
        Order order = findOrderById(orderId);
        validateUpdatePermission(order, userId, role);

        order.update(
                request.getSupplierId(),
                request.getReceiverId(),
                request.getProductId(),
                request.getQuantity(),
                request.getRequirements(),
                request.getDesiredDeliveryDate()
        );

        return OrderResponse.from(order);
    }

    /**
     * 3. 주문 취소.
     * - MASTER, HUB_MANAGER: 무조건 가능
     * - COMPANY_MANAGER: 본인 업체의 PENDING/ACCEPTED 주문만 가능
     * - DELIVERY_MANAGER: 취소 불가
     */
    @Transactional
    public OrderResponse cancelOrder(UUID orderId, String userId, String username, String role) {
        Order order = findOrderById(orderId);

        if ("DELIVERY_MANAGER".equals(role)) {
            throw new CustomException(ErrorCode.ORDER_UPDATE_FORBIDDEN);
        }
        if ("COMPANY_MANAGER".equals(role)) {
            UUID userCompanyId = getUserCompanyId(userId);
            if (!userCompanyId.equals(order.getSupplierId())) {
                throw new CustomException(ErrorCode.ORDER_UPDATE_FORBIDDEN);
            }
        }

        if (OrderStatus.CANCELLED.equals(order.getStatus())) {
            throw new CustomException(ErrorCode.ORDER_ALREADY_CANCELLED);
        }

        order.cancel();

        // DB 커밋 후에 Kafka 발행 — 롤백 시 이벤트 발행 방지
        OrderCancelledEvent cancelledEvent = new OrderCancelledEvent(
                order.getId(),
                order.getProductId(),
                order.getQuantity()
        );
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                orderEventProducer.publishOrderCancelled(cancelledEvent);
            }
        });

        return OrderResponse.from(order);
    }

    /**
     * 4. 주문 삭제 (Soft Delete).
     * - MASTER, HUB_MANAGER: 가능
     * - COMPANY_MANAGER, DELIVERY_MANAGER: 불가
     */
    @Transactional
    public void deleteOrder(UUID orderId, String userId, String username, String role) {
        Order order = findOrderById(orderId);

        switch (role) {
            case "MASTER" -> { /* 무조건 가능 */ }
            case "HUB_MANAGER" -> {
                UUID userHubId = getUserHubId(userId);
                if (!userHubId.equals(order.getHubId())) {
                    throw new CustomException(ErrorCode.ORDER_DELETE_FORBIDDEN);
                }
            }
            default -> throw new CustomException(ErrorCode.ORDER_DELETE_FORBIDDEN);
        }

        order.softDelete(username);
    }

    /**
     * 5. 주문 단건 조회.
     */
    public OrderResponse getOrder(UUID orderId, String userId, String role) {
        Order order = findOrderById(orderId);
        validateReadPermission(order, userId, role);
        return OrderResponse.from(order);
    }

    /**
     * 6. 주문 목록 검색 (QueryDSL).
     */
    public Page<OrderResponse> searchOrders(UUID supplierId, UUID receiverId, UUID hubId,
                                             OrderStatus status, String userId, String role,
                                             Pageable pageable) {
        UUID filterHubId = hubId;
        UUID filterSupplierId = supplierId;

        switch (role) {
            case "HUB_MANAGER" -> {
                UUID userHubId = getUserHubId(userId);
                filterHubId = userHubId;
            }
            case "COMPANY_MANAGER" -> {
                UUID userCompanyId = getUserCompanyId(userId);
                filterSupplierId = userCompanyId;
            }
        }

        return orderRepository.searchOrders(filterSupplierId, receiverId, filterHubId, status, pageable)
                .map(OrderResponse::from);
    }

    // ── Private 도움 메서드 ──

    /**
     * 7. 업체의 진행 중인 주문 건수 조회 (내부 API용).
     */
    public long countActiveOrdersByCompanyId(UUID companyId) {
        return orderRepository.countActiveOrdersByCompanyId(companyId);
    }

    private Order findOrderById(UUID orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));
    }

    private void validateUpdatePermission(Order order, String userId, String role) {
        switch (role) {
            case "MASTER" -> { /* 무조건 가능 */ }
            case "HUB_MANAGER" -> {
                UUID userHubId = getUserHubId(userId);
                if (!userHubId.equals(order.getHubId())) {
                    throw new CustomException(ErrorCode.ORDER_UPDATE_FORBIDDEN);
                }
            }
            case "COMPANY_MANAGER" -> {
                UUID userCompanyId = getUserCompanyId(userId);
                if (!userCompanyId.equals(order.getSupplierId())) {
                    throw new CustomException(ErrorCode.ORDER_UPDATE_FORBIDDEN);
                }
                if (!OrderStatus.PENDING.equals(order.getStatus())) {
                    throw new CustomException(ErrorCode.ORDER_UPDATE_FORBIDDEN);
                }
            }
            default -> throw new CustomException(ErrorCode.ORDER_UPDATE_FORBIDDEN);
        }
    }

    private void validateReadPermission(Order order, String userId, String role) {
        switch (role) {
            case "MASTER" -> { /* 무조건 가능 */ }
            case "HUB_MANAGER" -> {
                UUID userHubId = getUserHubId(userId);
                if (!userHubId.equals(order.getHubId())) {
                    throw new CustomException(ErrorCode.FORBIDDEN);
                }
            }
            case "COMPANY_MANAGER" -> {
                UUID userCompanyId = getUserCompanyId(userId);
                if (!userCompanyId.equals(order.getSupplierId())
                        && !userCompanyId.equals(order.getReceiverId())) {
                    throw new CustomException(ErrorCode.FORBIDDEN);
                }
            }
            case "DELIVERY_MANAGER" -> { /* 조회는 허용 */ }
            default -> throw new CustomException(ErrorCode.FORBIDDEN);
        }
    }

    private UUID getUserHubId(String userId) {
        return userClient.getMyInfo(userId).getData().getHubId();
    }

    private UUID getUserCompanyId(String userId) {
        return userClient.getMyInfo(userId).getData().getCompanyId();
    }
}
