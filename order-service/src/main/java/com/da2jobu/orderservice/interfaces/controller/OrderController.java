package com.da2jobu.orderservice.interfaces.controller;

import com.da2jobu.orderservice.application.service.OrderService;
import com.da2jobu.orderservice.domain.model.OrderStatus;
import com.da2jobu.orderservice.interfaces.dto.request.OrderCreateRequest;
import com.da2jobu.orderservice.interfaces.dto.request.OrderUpdateRequest;
import com.da2jobu.orderservice.interfaces.dto.response.OrderResponse;
import common.dto.CommonResponse;
import common.exception.CustomException;
import common.exception.ErrorCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    private static final Set<Integer> ALLOWED_PAGE_SIZES = Set.of(10, 30, 50);

    /**
     * 1. 주문 생성.
     */
    @PostMapping
    public ResponseEntity<CommonResponse<OrderResponse>> createOrder(
            @Valid @RequestBody OrderCreateRequest request,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-Username") String username,
            @RequestHeader("X-User-Role") String role) {

        OrderResponse response = orderService.createOrder(request, userId, username, role);
        return CommonResponse.created("주문이 생성되었습니다.", response);
    }

    /**
     * 2. 주문 수정.
     */
    @PatchMapping("/{orderId}")
    public ResponseEntity<CommonResponse<OrderResponse>> updateOrder(
            @PathVariable UUID orderId,
            @Valid @RequestBody OrderUpdateRequest request,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-Username") String username,
            @RequestHeader("X-User-Role") String role) {

        OrderResponse response = orderService.updateOrder(orderId, request, userId, username, role);
        return CommonResponse.ok("주문이 수정되었습니다.", response);
    }

    /**
     * 3. 주문 취소.
     */
    @PatchMapping("/{orderId}/cancel")
    public ResponseEntity<CommonResponse<OrderResponse>> cancelOrder(
            @PathVariable UUID orderId,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-Username") String username,
            @RequestHeader("X-User-Role") String role) {

        OrderResponse response = orderService.cancelOrder(orderId, userId, username, role);
        return CommonResponse.ok("주문이 취소되었습니다.", response);
    }

    /**
     * 4. 주문 삭제 (Soft Delete).
     */
    @DeleteMapping("/{orderId}")
    public ResponseEntity<CommonResponse<?>> deleteOrder(
            @PathVariable UUID orderId,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-Username") String username,
            @RequestHeader("X-User-Role") String role) {

        orderService.deleteOrder(orderId, userId, username, role);
        return CommonResponse.ok("주문이 삭제되었습니다.");
    }

    /**
     * 5. 주문 단건 조회.
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<CommonResponse<OrderResponse>> getOrder(
            @PathVariable UUID orderId,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Role") String role) {

        OrderResponse response = orderService.getOrder(orderId, userId, role);
        return CommonResponse.ok(response);
    }

    /**
     * 6. 주문 목록 검색.
     */
    @GetMapping
    public ResponseEntity<CommonResponse<Page<OrderResponse>>> searchOrders(
            @RequestParam(required = false) UUID supplierId,
            @RequestParam(required = false) UUID receiverId,
            @RequestParam(required = false) UUID hubId,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Role") String role) {

        validatePageSize(size);
        validatePageNumber(page);
        validateDirection(direction);
        Sort sort = Sort.by(Sort.Direction.fromString(direction), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<OrderResponse> response = orderService.searchOrders(
                supplierId, receiverId, hubId, status, userId, role, pageable);
        return CommonResponse.ok(response);
    }

    /**
     * 7. 업체의 진행 중인 주문 건수 조회 (내부 API — company-service 전용).
     * PENDING, ACCEPTED 상태인 주문만 카운트.
     */
    @GetMapping("/internal/active-count")
    public ResponseEntity<CommonResponse<Long>> countActiveOrders(
            @RequestParam UUID companyId) {

        long count = orderService.countActiveOrdersByCompanyId(companyId);
        return CommonResponse.ok(count);
    }

    private void validatePageSize(int size) {
        if (!ALLOWED_PAGE_SIZES.contains(size)) {
            throw new CustomException(ErrorCode.INVALID_PAGE_SIZE);
        }
    }

    private void validatePageNumber(int page) {
        if (page < 0) {
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }
    }

    private void validateDirection(String direction) {
        if (Sort.Direction.fromOptionalString(direction).isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }
    }
}
