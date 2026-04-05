package com.da2jobu.productservice.domain.repository;

import com.da2jobu.productservice.domain.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * 상품 리포지토리.
 */
public interface ProductRepository extends JpaRepository<Product, UUID>, ProductRepositoryCustom {
}
