package com.da2jobu.productservice.domain.repository;

import com.da2jobu.productservice.domain.model.ProductPriceHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * 상품 가격 변동 이력 리포지토리.
 */
public interface ProductPriceHistoryRepository extends JpaRepository<ProductPriceHistory, UUID> {
    // product.id 기준으로 변경일시(changedAt) 내림차순 조회
    Page<ProductPriceHistory> findByProduct_IdOrderByChangedAtDesc(UUID productId, Pageable pageable);
}
