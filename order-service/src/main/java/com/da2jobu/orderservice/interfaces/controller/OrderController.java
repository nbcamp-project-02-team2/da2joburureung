package com.da2jobu.orderservice.interfaces.controller;

import com.da2jobu.orderservice.application.service.OrderService;
import com.da2jobu.orderservice.domain.model.OrderStatus;
import com.da2jobu.orderservice.interfaces.dto.request.OrderCreateRequest;
import com.da2jobu.orderservice.interfaces.dto.request.OrderUpdateRequest;
import com.da2jobu.orderservice.interfaces.dto.response.OrderResponse;
import common.dto.CommonResponse;
import common.exception.CustomException;
import common.exception.ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Order", description = "주문 관리 API")
public class OrderController {

    private final OrderService orderService;

    private static final Set<Integer> ALLOWED_PAGE_SIZES = Set.of(10, 30, 50);

    /**
     * 1. 주문 생성.
     */
    @Operation(summary = "주문 생성", description = "새 주문을 생성합니다.")
    @PostMapping
    public ResponseEntity<CommonResponse<OrderResponse>> createOrder(
            @Valid @RequestBody OrderCreateRequest request,
            @Parameter(description = "요청 사용자 ID", example = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
            @RequestHeader("X-User-Id") String userId,
            @Parameter(description = "요청 사용자명", example = "홍길동")
            @RequestHeader("X-Username") String username,
            @Parameter(description = "요청 사용자 역할", example = "COMPANY_MANAGER")
            @RequestHeader("X-User-Role") String role) {

        OrderResponse response = orderService.createOrder(request, userId, username, role);
        return CommonResponse.created("주문이 생성되었습니다.", response);
    }

    /**
     * 2. 주문 수정.
     */
    @Operation(summary = "주문 수정", description = "기존 주문 정보를 수정합니다.")
    @PatchMapping("/{orderId}")
    public ResponseEntity<CommonResponse<OrderResponse>> updateOrder(
            @Parameter(description = "주문 ID", example = "11111111-1111-1111-1111-111111111111")
            @PathVariable UUID orderId,
            @Valid @RequestBody OrderUpdateRequest request,
            @Parameter(description = "요청 사용자 ID", example = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
            @RequestHeader("X-User-Id") String userId,
            @Parameter(description = "요청 사용자명", example = "홍길동")
            @RequestHeader("X-Username") String username,
            @Parameter(description = "요청 사용자 역할", example = "COMPANY_MANAGER")
            @RequestHeader("X-User-Role") String role) {

        OrderResponse response = orderService.updateOrder(orderId, request, userId, username, role);
        return CommonResponse.ok("주문이 수정되었습니다.", response);
    }

    /**
     * 3. 주문 취소.
     */
    @Operation(summary = "주문 취소", description = "주문을 취소합니다.")
    @PatchMapping("/{orderId}/cancel")
    public ResponseEntity<CommonResponse<OrderResponse>> cancelOrder(
            @Parameter(description = "주문 ID", example = "11111111-1111-1111-1111-111111111111")
            @PathVariable UUID orderId,
            @Parameter(description = "요청 사용자 ID", example = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
            @RequestHeader("X-User-Id") String userId,
            @Parameter(description = "요청 사용자명", example = "홍길동")
            @RequestHeader("X-Username") String username,
            @Parameter(description = "요청 사용자 역할", example = "COMPANY_MANAGER")
            @RequestHeader("X-User-Role") String role) {

        OrderResponse response = orderService.cancelOrder(orderId, userId, username, role);
        return CommonResponse.ok("주문이 취소되었습니다.", response);
    }

    /**
     * 4. 주문 삭제 (Soft Delete).
     */
    @Operation(summary = "주문 삭제", description = "주문을 소프트 삭제합니다.")
    @DeleteMapping("/{orderId}")
    public ResponseEntity<CommonResponse<?>> deleteOrder(
            @Parameter(description = "주문 ID", example = "11111111-1111-1111-1111-111111111111")
            @PathVariable UUID orderId,
            @Parameter(description = "요청 사용자 ID", example = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
            @RequestHeader("X-User-Id") String userId,
            @Parameter(description = "요청 사용자명", example = "홍길동")
            @RequestHeader("X-Username") String username,
            @Parameter(description = "요청 사용자 역할", example = "MASTER")
            @RequestHeader("X-User-Role") String role) {

        orderService.deleteOrder(orderId, userId, username, role);
        return CommonResponse.ok("주문이 삭제되었습니다.");
    }

    /**
     * 5. 주문 단건 조회.
     */
    @Operation(summary = "주문 단건 조회", description = "주문 1건을 조회합니다.")
    @GetMapping("/{orderId}")
    public ResponseEntity<CommonResponse<OrderResponse>> getOrder(
            @Parameter(description = "주문 ID", example = "11111111-1111-1111-1111-111111111111")
            @PathVariable UUID orderId,
            @Parameter(description = "요청 사용자 ID", example = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
            @RequestHeader("X-User-Id") String userId,
            @Parameter(description = "요청 사용자 역할", example = "COMPANY_MANAGER")
            @RequestHeader("X-User-Role") String role) {

        OrderResponse response = orderService.getOrder(orderId, userId, role);
        return CommonResponse.ok(response);
    }

    /**
     * 6. 주문 목록 검색.
     */
    @Operation(summary = "주문 목록 조회", description = "조건에 맞는 주문 목록을 페이징 조회합니다.")
    @GetMapping
    public ResponseEntity<CommonResponse<Page<OrderResponse>>> searchOrders(
            @Parameter(description = "공급 업체 ID", example = "22222222-2222-2222-2222-222222222222")
            @RequestParam(required = false) UUID supplierId,
            @Parameter(description = "수령자 ID", example = "33333333-3333-3333-3333-333333333333")
            @RequestParam(required = false) UUID receiverId,
            @Parameter(description = "허브 ID", example = "44444444-4444-4444-4444-444444444444")
            @RequestParam(required = false) UUID hubId,
            @Parameter(description = "주문 상태", example = "PENDING")
            @RequestParam(required = false) OrderStatus status,
            @Parameter(description = "페이지 번호", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "정렬 기준 필드", example = "createdAt")
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "정렬 방향", example = "desc")
            @RequestParam(defaultValue = "desc") String direction,
            @Parameter(description = "요청 사용자 ID", example = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
            @RequestHeader("X-User-Id") String userId,
            @Parameter(description = "요청 사용자 역할", example = "HUB_MANAGER")
            @RequestHeader("X-User-Role") String role
    ) {
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
    @Operation(
            summary = "업체 진행 중 주문 건수 조회",
            description = "company-service 내부 호출용으로 진행 중인 주문 건수를 조회합니다."
    )
    @GetMapping("/internal/active-count")
    public ResponseEntity<CommonResponse<Long>> countActiveOrders(
            @Parameter(description = "업체 ID", example = "22222222-2222-2222-2222-222222222222")
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
