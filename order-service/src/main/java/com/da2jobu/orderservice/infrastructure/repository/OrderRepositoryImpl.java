package com.da2jobu.orderservice.infrastructure.repository;

import com.da2jobu.orderservice.domain.model.Order;
import com.da2jobu.orderservice.domain.model.OrderStatus;
import com.da2jobu.orderservice.domain.model.QOrder;
import com.da2jobu.orderservice.domain.repository.OrderRepositoryCustom;
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

@Repository
@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Order> searchOrders(UUID supplierId, UUID receiverId, UUID hubId,
                                     OrderStatus status, Pageable pageable) {
        QOrder order = QOrder.order;
        BooleanBuilder builder = new BooleanBuilder();

        if (supplierId != null) {
            builder.and(order.supplierId.eq(supplierId));
        }
        if (receiverId != null) {
            builder.and(order.receiverId.eq(receiverId));
        }
        if (hubId != null) {
            builder.and(order.hubId.eq(hubId));
        }
        if (status != null) {
            builder.and(order.status.eq(status));
        }

        JPAQuery<Order> query = queryFactory
                .selectFrom(order)
                .where(builder)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize());

        for (Sort.Order sortOrder : pageable.getSort()) {
            query.orderBy(getOrderSpecifier(sortOrder, order));
        }

        List<Order> content = query.fetch();

        Long total = queryFactory
                .select(order.count())
                .from(order)
                .where(builder)
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    @Override
    public long countActiveOrdersByCompanyId(UUID companyId) {
        QOrder order = QOrder.order;
        Long count = queryFactory
                .select(order.count())
                .from(order)
                .where(
                        order.supplierId.eq(companyId).or(order.receiverId.eq(companyId)),
                        order.status.in(OrderStatus.PENDING, OrderStatus.ACCEPTED)
                )
                .fetchOne();
        return count != null ? count : 0L;
    }

    private OrderSpecifier<?> getOrderSpecifier(Sort.Order sortOrder, QOrder order) {
        com.querydsl.core.types.Order direction = sortOrder.isAscending()
                ? com.querydsl.core.types.Order.ASC
                : com.querydsl.core.types.Order.DESC;

        return switch (sortOrder.getProperty()) {
            case "status" -> new OrderSpecifier<>(direction, order.status);
            case "quantity" -> new OrderSpecifier<>(direction, order.quantity);
            case "updatedAt" -> new OrderSpecifier<>(direction, order.updatedAt);
            default -> new OrderSpecifier<>(direction, order.createdAt);
        };
    }
}
