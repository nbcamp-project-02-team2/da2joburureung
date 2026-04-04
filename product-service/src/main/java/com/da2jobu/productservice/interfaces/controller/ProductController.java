package com.da2jobu.productservice.interfaces.controller;

import com.da2jobu.productservice.application.service.ProductService;
import com.da2jobu.productservice.interfaces.dto.request.ProductCreateRequest;
import com.da2jobu.productservice.interfaces.dto.request.ProductUpdateRequest;
import com.da2jobu.productservice.interfaces.dto.response.ProductPriceHistoryResponse;
import com.da2jobu.productservice.interfaces.dto.response.ProductResponse;
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

/**
 * 상품 API 컨트롤러.
 * - Gateway에서 전달하는 X-User-Id, X-Username, X-User-Role 헤더로 인증/권한 처리
 * - 페이지 크기: 10, 30, 50건만 허용 (기본 10건)
 */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    private static final Set<Integer> ALLOWED_PAGE_SIZES = Set.of(10, 30, 50);

    /**
     * 1. 상품 생성.
     * - 권한: MASTER, HUB_MANAGER(담당 허브), COMPANY_MANAGER(본인 업체)
     */
    @PostMapping
    public ResponseEntity<CommonResponse<ProductResponse>> createProduct(
            @Valid @RequestBody ProductCreateRequest request,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-Username") String username,
            @RequestHeader("X-User-Role") String role) {

        ProductResponse response = productService.createProduct(request, userId, username, role);
        return CommonResponse.created("상품이 생성되었습니다.", response);
    }

    /**
     * 2. 상품 수정 (부분 수정 지원).
     * - 권한: MASTER, HUB_MANAGER(담당 허브), COMPANY_MANAGER(본인 업체)
     * - 가격 변경 시 자동으로 가격 이력 기록
     */
    @PatchMapping("/{productId}")
    public ResponseEntity<CommonResponse<ProductResponse>> updateProduct(
            @PathVariable UUID productId,
            @Valid @RequestBody ProductUpdateRequest request,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-Username") String username,
            @RequestHeader("X-User-Role") String role) {

        ProductResponse response = productService.updateProduct(productId, request, userId, username, role);
        return CommonResponse.ok("상품이 수정되었습니다.", response);
    }

    /**
     * 3. 상품 삭제 (Soft Delete).
     * - 권한: MASTER, HUB_MANAGER(담당 허브)만 가능
     * - 삭제된 상품은 이후 조회/검색에서 자동 제외
     */
    @DeleteMapping("/{productId}")
    public ResponseEntity<CommonResponse<?>> deleteProduct(
            @PathVariable UUID productId,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-Username") String username,
            @RequestHeader("X-User-Role") String role) {

        productService.deleteProduct(productId, userId, username, role);
        return CommonResponse.ok("상품이 삭제되었습니다.");
    }

    /**
     * 4. 상품 단건 조회.
     * - 권한: 모든 인증된 사용자
     */
    @GetMapping("/{productId}")
    public ResponseEntity<CommonResponse<ProductResponse>> getProduct(
            @PathVariable UUID productId) {

        ProductResponse response = productService.getProduct(productId);
        return CommonResponse.ok(response);
    }

    /**
     * 5. 상품 목록 조회 및 검색 (QueryDSL).
     * - 권한: 모든 인증된 사용자
     * - 필터: name(상품명), hubId(허브), companyId(업체)
     * - 정렬: name, price, stockQuantity, updatedAt, createdAt(기본값)
     */
    @GetMapping
    public ResponseEntity<CommonResponse<Page<ProductResponse>>> searchProducts(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) UUID hubId,
            @RequestParam(required = false) UUID companyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        validatePageSize(size);
        validatePageNumber(page);
        validateDirection(direction);
        Sort sort = Sort.by(Sort.Direction.fromString(direction), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<ProductResponse> response = productService.searchProducts(name, hubId, companyId, pageable);
        return CommonResponse.ok(response);
    }

    /**
     * 6. 상품 가격 이력 조회.
     * - 권한: 모든 인증된 사용자
     * - 변경일시(changedAt) 기준 내림차순 정렬
     */
    @GetMapping("/{productId}/price-histories")
    public ResponseEntity<CommonResponse<Page<ProductPriceHistoryResponse>>> getPriceHistories(
            @PathVariable UUID productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        validatePageSize(size);
        validatePageNumber(page);
        Pageable pageable = PageRequest.of(page, size);

        Page<ProductPriceHistoryResponse> response = productService.getPriceHistories(productId, pageable);
        return CommonResponse.ok(response);
    }

    /** 페이지 크기 유효성 검증 (10, 30, 50만 허용) */
    private void validatePageSize(int size) {
        if (!ALLOWED_PAGE_SIZES.contains(size)) {
            throw new CustomException(ErrorCode.INVALID_PAGE_SIZE);
        }
    }

    /** 페이지 번호 유효성 검증 (0 이상) */
    private void validatePageNumber(int page) {
        if (page < 0) {
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }
    }

    /** 정렬 방향 유효성 검증 (asc/desc만 허용) */
    private void validateDirection(String direction) {
        if (Sort.Direction.fromOptionalString(direction).isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }
    }
}
