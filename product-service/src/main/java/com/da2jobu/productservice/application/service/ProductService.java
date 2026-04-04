package com.da2jobu.productservice.application.service;

import com.da2jobu.productservice.domain.model.Product;
import com.da2jobu.productservice.domain.repository.ProductPriceHistoryRepository;
import com.da2jobu.productservice.domain.repository.ProductRepository;
import com.da2jobu.productservice.infrastructure.client.CompanyClient;
import com.da2jobu.productservice.infrastructure.client.CompanyInfoResponse;
import com.da2jobu.productservice.infrastructure.client.UserClient;
import com.da2jobu.productservice.infrastructure.client.UserInfoResponse;
import com.da2jobu.productservice.interfaces.dto.request.ProductCreateRequest;
import com.da2jobu.productservice.interfaces.dto.request.ProductUpdateRequest;
import com.da2jobu.productservice.interfaces.dto.response.ProductPriceHistoryResponse;
import com.da2jobu.productservice.interfaces.dto.response.ProductResponse;
import common.exception.CustomException;
import common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * 상품 비즈니스 유스케이스 서비스.
 * - RBAC 기반 권한 검증 (User 서비스에서 hubId/companyId 조회 후 비교)
 * - FeignClient를 통한 업체 유효성 검증
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductPriceHistoryRepository priceHistoryRepository;
    private final CompanyClient companyClient;
    private final UserClient userClient;

    /**
     * 1. 상품 생성.
     * - 마스터: 무조건 가능
     * - 허브 관리자: 담당 허브에 포함된 업체의 상품만 생성 가능
     * - 업체 담당자: 본인 업체의 상품만 생성 가능
     * - 배송 담당자: 생성 불가
     */
    @Transactional
    public ProductResponse createProduct(ProductCreateRequest request,
                                          String userId, String username, String role) {
        UUID companyId;
        UUID hubId;

        switch (role) {
            case "MASTER" -> {
                if (request.getCompanyId() == null || request.getHubId() == null) {
                    throw new CustomException(ErrorCode.INVALID_INPUT);
                }
                companyId = request.getCompanyId();
                hubId = request.getHubId();
                validateCompany(companyId, hubId);
            }
            case "HUB_MANAGER" -> {
                if (request.getCompanyId() == null) {
                    throw new CustomException(ErrorCode.INVALID_INPUT);
                }
                hubId = getUserHubId(userId);
                companyId = request.getCompanyId();
                validateCompany(companyId, hubId);
            }
            case "COMPANY_MANAGER" -> {
                companyId = getUserCompanyId(userId);
                CompanyInfoResponse company = companyClient.getCompany(companyId).getData();
                hubId = company.getHubId();
            }
            default -> throw new CustomException(ErrorCode.PRODUCT_CREATE_FORBIDDEN);
        }

        Product product = Product.builder()
                .name(request.getName())
                .price(request.getPrice())
                .stockQuantity(request.getStockQuantity())
                .hubId(hubId)
                .companyId(companyId)
                .description(request.getDescription())
                .build();

        return ProductResponse.from(productRepository.save(product));
    }

    /**
     * 2. 상품 수정.
     * - 마스터: 무조건 가능
     * - 허브 관리자: 담당 허브에 포함된 업체의 상품만 수정 가능
     * - 업체 담당자: 본인 업체의 상품만 수정 가능
     * - 배송 담당자: 수정 불가
     *
     * 가격 변경 시 Product 엔티티 내부에서 자동으로 PriceHistory 이력 기록
     */
    @Transactional
    public ProductResponse updateProduct(UUID productId, ProductUpdateRequest request,
                                          String userId, String username, String role) {
        Product product = findProductById(productId);
        validateUpdatePermission(product, userId, role);

        product.update(
                request.getName(),
                request.getPrice(),
                request.getStockQuantity(),
                request.getIsVisible(),
                request.getDescription(),
                request.getReason(),
                username
        );

        return ProductResponse.from(product);
    }

    /**
     * 3. 상품 삭제 (Soft Delete).
     * - 마스터: 무조건 가능
     * - 허브 관리자: 담당 허브에 포함된 업체 상품만 삭제 가능
     * - 업체 담당자: 삭제 불가
     * - 배송 담당자: 삭제 불가
     */
    @Transactional
    public void deleteProduct(UUID productId, String userId, String username, String role) {
        Product product = findProductById(productId);
        validateDeletePermission(product, userId, role);
        product.softDelete(username);
    }

    /**
     * 4. 상품 단건 조회.
     * - 모든 인증된 사용자 가능
     */
    public ProductResponse getProduct(UUID productId) {
        return ProductResponse.from(findProductById(productId));
    }

    /**
     * 5. 상품 목록 조회 및 검색 (QueryDSL).
     * - 모든 인증된 사용자 가능
     * - 상품명, 허브 ID, 업체 ID 기준 필터링
     */
    public Page<ProductResponse> searchProducts(String name, UUID hubId, UUID companyId,
                                                 Pageable pageable) {
        return productRepository.searchProducts(name, hubId, companyId, pageable)
                .map(ProductResponse::from);
    }

    /**
     * 6. 상품 가격 이력 조회.
     * - 변경일시(changedAt) 기준 내림차순 정렬
     */
    public Page<ProductPriceHistoryResponse> getPriceHistories(UUID productId, Pageable pageable) {
        findProductById(productId);
        return priceHistoryRepository.findByProduct_IdOrderByChangedAtDesc(productId, pageable)
                .map(ProductPriceHistoryResponse::from);
    }

    // ── Private: 엔티티 조회 ──

    private Product findProductById(UUID productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));
    }

    // ── Private: FeignClient 유효성 검증 ──

    /**
     * 업체 존재 여부 + 허브 소속 검증 (1회 호출로 통합).
     * - company-service 404 → Circuit Breaker fallback → COMPANY_SERVICE_UNAVAILABLE
     * - hubId 불일치 → PRODUCT_COMPANY_HUB_MISMATCH
     */
    private void validateCompany(UUID companyId, UUID hubId) {
        CompanyInfoResponse company = companyClient.getCompany(companyId).getData();
        if (!hubId.equals(company.getHubId())) {
            throw new CustomException(ErrorCode.PRODUCT_COMPANY_HUB_MISMATCH);
        }
    }

    // ── Private: RBAC 권한 검증 ──
    // User 테이블에 hub_id, company_id가 있으므로
    // User-service에서 사용자의 소속 정보를 조회하여 상품의 소속 정보와 직접 비교

    /** 상품 수정 권한 검증 */
    private void validateUpdatePermission(Product product, String userId, String role) {
        switch (role) {
            case "MASTER" -> { /* 무조건 가능 */ }
            case "HUB_MANAGER" -> {
                UUID userHubId = getUserHubId(userId);
                if (!product.getHubId().equals(userHubId)) {
                    throw new CustomException(ErrorCode.PRODUCT_UPDATE_FORBIDDEN);
                }
            }
            case "COMPANY_MANAGER" -> {
                UUID userCompanyId = getUserCompanyId(userId);
                if (!product.getCompanyId().equals(userCompanyId)) {
                    throw new CustomException(ErrorCode.PRODUCT_UPDATE_FORBIDDEN);
                }
            }
            default -> throw new CustomException(ErrorCode.PRODUCT_UPDATE_FORBIDDEN);
        }
    }

    /** 상품 삭제 권한 검증 (업체 담당자, 배송 담당자 삭제 불가) */
    private void validateDeletePermission(Product product, String userId, String role) {
        switch (role) {
            case "MASTER" -> { /* 무조건 가능 */ }
            case "HUB_MANAGER" -> {
                UUID userHubId = getUserHubId(userId);
                if (!product.getHubId().equals(userHubId)) {
                    throw new CustomException(ErrorCode.PRODUCT_DELETE_FORBIDDEN);
                }
            }
            default -> throw new CustomException(ErrorCode.PRODUCT_DELETE_FORBIDDEN);
        }
    }

    /** User-service에서 사용자의 담당 허브 ID 조회 */
    private UUID getUserHubId(String userId) {
        return userClient.getMyInfo(userId).getData().getHubId();
    }

    /** User-service에서 사용자의 소속 업체 ID 조회 */
    private UUID getUserCompanyId(String userId) {
        return userClient.getMyInfo(userId).getData().getCompanyId();
    }
}
