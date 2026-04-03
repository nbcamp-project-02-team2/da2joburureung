package com.delivery.hub.domain.repository;

import com.delivery.hub.application.dto.SearchHubCommand;
import com.delivery.hub.domain.model.Hub;
import com.delivery.hub.domain.model.QHub;
import com.delivery.hub.interfaces.dto.Respone.HubResponse;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;

import java.util.List;

@RequiredArgsConstructor
public class HubRepositoryImpl implements HubRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<HubResponse> searchHubs(SearchHubCommand command, Pageable pageable) {
        QHub hub = QHub.hub;

        List<Hub> content = queryFactory
                .selectFrom(hub)
                .where(
                        nameContains(command.hub_name()),
                        addressContains(command.address()),
                        isNotDeleted()
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(hub.createdAt.desc())
                .fetch();

        // 전체 개수 조회
        Long total = queryFactory
                .select(hub.count())
                .from(hub)
                .where(
                        nameContains(command.hub_name()),
                        addressContains(command.address()),
                        isNotDeleted()
                )
                .fetchOne();


        List<HubResponse> dtoList = content.stream()
                .map(HubResponse::from)
                .toList();

        // total이 null일 경우를 대비해 0L 처리
        return new PageImpl<>(dtoList, pageable, total != null ? total : 0L);
    }

    private BooleanExpression isNotDeleted() {
        return QHub.hub.deletedAt.isNull();
    }

    private BooleanExpression nameContains(String hubName) {
        return StringUtils.hasText(hubName) ? QHub.hub.hubName.contains(hubName) : null;
    }

    private BooleanExpression addressContains(String address) {
        return StringUtils.hasText(address) ? QHub.hub.address.contains(address) : null;
    }
}