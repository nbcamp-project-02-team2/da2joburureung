package com.da2jobu.productservice.domain.repository;

import com.da2jobu.productservice.domain.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * QueryDSL 기반 상품 동적 검색 인터페이스.
 */
public interface ProductRepositoryCustom {

    /**
     * 상품 목록 검색.
     * @param name      상품명 (부분 일치, nullable)
     * @param hubId     허브 ID 필터 (nullable)
     * @param companyId 업체 ID 필터 (nullable)
     * @param pageable  페이징 및 정렬 정보
     */
    Page<Product> searchProducts(String name, UUID hubId, UUID companyId, Pageable pageable);
}
