package com.da2jobu.productservice.interfaces.controller;

import com.da2jobu.productservice.application.service.ProductService;
import com.da2jobu.productservice.interfaces.dto.request.ProductCreateRequest;
import com.da2jobu.productservice.interfaces.dto.request.ProductUpdateRequest;
import com.da2jobu.productservice.interfaces.dto.response.ProductPriceHistoryResponse;
import com.da2jobu.productservice.interfaces.dto.response.ProductResponse;
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

/**
 * 상품 API 컨트롤러.
 * - Gateway에서 전달하는 X-User-Id, X-Username, X-User-Role 헤더로 인증/권한 처리
 * - 페이지 크기: 10, 30, 50건만 허용 (기본 10건)
 */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Product", description = "상품 관리 API")
public class ProductController {

    private final ProductService productService;

    private static final Set<Integer> ALLOWED_PAGE_SIZES = Set.of(10, 30, 50);

    /**
     * 1. 상품 생성.
     * - 권한: MASTER, HUB_MANAGER(담당 허브), COMPANY_MANAGER(본인 업체)
     */
    @Operation(summary = "상품 생성", description = "새 상품을 생성합니다.")
    @PostMapping
    public ResponseEntity<CommonResponse<ProductResponse>> createProduct(
            @Valid @RequestBody ProductCreateRequest request,
            @Parameter(description = "요청 사용자 ID", example = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
            @RequestHeader("X-User-Id") String userId,
            @Parameter(description = "요청 사용자명", example = "홍길동")
            @RequestHeader("X-Username") String username,
            @Parameter(description = "요청 사용자 역할", example = "COMPANY_MANAGER")
            @RequestHeader("X-User-Role") String role) {

        ProductResponse response = productService.createProduct(request, userId, username, role);
        return CommonResponse.created("상품이 생성되었습니다.", response);
    }

    /**
     * 2. 상품 수정 (부분 수정 지원).
     * - 권한: MASTER, HUB_MANAGER(담당 허브), COMPANY_MANAGER(본인 업체)
     * - 가격 변경 시 자동으로 가격 이력 기록
     */
    @Operation(summary = "상품 수정", description = "기존 상품 정보를 부분 수정합니다.")
    @PatchMapping("/{productId}")
    public ResponseEntity<CommonResponse<ProductResponse>> updateProduct(
            @Parameter(description = "상품 ID", example = "11111111-1111-1111-1111-111111111111")
            @PathVariable UUID productId,
            @Valid @RequestBody ProductUpdateRequest request,
            @Parameter(description = "요청 사용자 ID", example = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
            @RequestHeader("X-User-Id") String userId,
            @Parameter(description = "요청 사용자명", example = "홍길동")
            @RequestHeader("X-Username") String username,
            @Parameter(description = "요청 사용자 역할", example = "COMPANY_MANAGER")
            @RequestHeader("X-User-Role") String role) {

        ProductResponse response = productService.updateProduct(productId, request, userId, username, role);
        return CommonResponse.ok("상품이 수정되었습니다.", response);
    }

    /**
     * 3. 상품 삭제 (Soft Delete).
     * - 권한: MASTER, HUB_MANAGER(담당 허브)만 가능
     * - 삭제된 상품은 이후 조회/검색에서 자동 제외
     */
    @Operation(summary = "상품 삭제", description = "상품을 소프트 삭제합니다.")
    @DeleteMapping("/{productId}")
    public ResponseEntity<CommonResponse<?>> deleteProduct(
            @Parameter(description = "상품 ID", example = "11111111-1111-1111-1111-111111111111")
            @PathVariable UUID productId,
            @Parameter(description = "요청 사용자 ID", example = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
            @RequestHeader("X-User-Id") String userId,
            @Parameter(description = "요청 사용자명", example = "홍길동")
            @RequestHeader("X-Username") String username,
            @Parameter(description = "요청 사용자 역할", example = "MASTER")
            @RequestHeader("X-User-Role") String role) {

        productService.deleteProduct(productId, userId, username, role);
        return CommonResponse.ok("상품이 삭제되었습니다.");
    }

    /**
     * 4. 상품 단건 조회.
     * - 권한: 모든 인증된 사용자
     */
    @Operation(summary = "상품 단건 조회", description = "상품 1건을 조회합니다.")
    @GetMapping("/{productId}")
    public ResponseEntity<CommonResponse<ProductResponse>> getProduct(
            @Parameter(description = "상품 ID", example = "11111111-1111-1111-1111-111111111111")
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
    @Operation(summary = "상품 목록 조회", description = "조건에 맞는 상품 목록을 페이징 조회합니다.")
    @GetMapping
    public ResponseEntity<CommonResponse<Page<ProductResponse>>> searchProducts(
            @Parameter(description = "상품명 검색어", example = "생수")
            @RequestParam(required = false) String name,
            @Parameter(description = "허브 ID", example = "22222222-2222-2222-2222-222222222222")
            @RequestParam(required = false) UUID hubId,
            @Parameter(description = "업체 ID", example = "33333333-3333-3333-3333-333333333333")
            @RequestParam(required = false) UUID companyId,
            @Parameter(description = "페이지 번호", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "정렬 기준 필드", example = "createdAt")
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "정렬 방향", example = "desc")
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
    @Operation(summary = "상품 가격 이력 조회", description = "특정 상품의 가격 변동 이력을 조회합니다.")
    @GetMapping("/{productId}/price-histories")
    public ResponseEntity<CommonResponse<Page<ProductPriceHistoryResponse>>> getPriceHistories(
            @Parameter(description = "상품 ID", example = "11111111-1111-1111-1111-111111111111")
            @PathVariable UUID productId,
            @Parameter(description = "페이지 번호", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "10")
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
