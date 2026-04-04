package com.da2jobu.productservice.infrastructure.repository;

import com.da2jobu.productservice.domain.model.Product;
import com.da2jobu.productservice.domain.model.QProduct;
import com.da2jobu.productservice.domain.repository.ProductRepositoryCustom;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * QueryDSL 기반 상품 동적 검색 구현체.
 * - 상품명(name), 허브 ID, 업체 ID 기준 필터링
 * - 정렬 필드: name, price, stockQuantity, updatedAt, createdAt(기본값)
 */
@Repository
@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Product> searchProducts(String name, UUID hubId, UUID companyId, Pageable pageable) {
        QProduct product = QProduct.product;

        BooleanBuilder builder = new BooleanBuilder();

        // 상품명 부분 일치 검색 (대소문자 무시)
        if (name != null && !name.isBlank()) {
            builder.and(product.name.containsIgnoreCase(name));
        }
        // 허브 ID 필터
        if (hubId != null) {
            builder.and(product.hubId.eq(hubId));
        }
        // 업체 ID 필터
        if (companyId != null) {
            builder.and(product.companyId.eq(companyId));
        }

        JPAQuery<Product> query = queryFactory
                .selectFrom(product)
                .where(builder)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize());

        // 동적 정렬 적용
        for (Sort.Order order : pageable.getSort()) {
            query.orderBy(getOrderSpecifier(order, product));
        }

        List<Product> content = query.fetch();

        // 전체 건수 조회
        Long total = queryFactory
                .select(product.count())
                .from(product)
                .where(builder)
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    /** 정렬 필드명 -> QueryDSL OrderSpecifier 변환 */
    private OrderSpecifier<?> getOrderSpecifier(Sort.Order order, QProduct product) {
        com.querydsl.core.types.Order direction = order.isAscending()
                ? com.querydsl.core.types.Order.ASC
                : com.querydsl.core.types.Order.DESC;

        return switch (order.getProperty()) {
            case "name" -> new OrderSpecifier<>(direction, product.name);
            case "price" -> new OrderSpecifier<>(direction, product.price);
            case "stockQuantity" -> new OrderSpecifier<>(direction, product.stockQuantity);
            case "updatedAt" -> new OrderSpecifier<>(direction, product.updatedAt);
            default -> new OrderSpecifier<>(direction, product.createdAt);
        };
    }
}
