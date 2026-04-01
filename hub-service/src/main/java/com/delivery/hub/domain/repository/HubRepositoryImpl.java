package com.delivery.hub.domain.repository;

import com.delivery.hub.application.dto.SearchHubCommand;
import com.delivery.hub.domain.model.Hub;
import com.delivery.hub.domain.model.QHub; // 빌드 시 생성된 Q클래스
import com.delivery.hub.interfaces.dto.Respone.HubResponse;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
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
                        addressContains(command.address())
                )
                .offset(pageable.getOffset())   // 몇 번째부터 시작할지
                .limit(pageable.getPageSize()) // 한 페이지에 몇 개 보여줄지
                .orderBy(hub.createdAt.desc()) // 기본 정렬 (최신순)
                .fetch();

        // 전체 개수 조회
        Long total = queryFactory
                .select(hub.count())
                .from(hub)
                .where(
                        nameContains(command.hub_name()),
                        addressContains(command.address())
                )
                .fetchOne();


        List<HubResponse> dtoList = content.stream()
                .map(HubResponse::from)
                .toList();

        // total이 null일 경우를 대비해 0L 처리
        return new PageImpl<>(dtoList, pageable, total != null ? total : 0L);
    }

    private BooleanExpression nameContains(String hub_name) {
        return StringUtils.hasText(hub_name) ? QHub.hub.hubName.contains(hub_name) : null;
    }

    private BooleanExpression addressContains(String address) {
        return StringUtils.hasText(address) ? QHub.hub.address.contains(address) : null;
    }
}