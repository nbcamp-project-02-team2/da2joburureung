package com.da2jobu.deliverymanagerservice.infrastructure.persistence;

import com.da2jobu.deliverymanagerservice.domain.model.entity.DeliveryManager;
import com.da2jobu.deliverymanagerservice.domain.model.entity.QDeliveryManager;
import com.da2jobu.deliverymanagerservice.domain.model.vo.DeliveryManagerId;
import com.da2jobu.deliverymanagerservice.domain.model.vo.DeliveryManagerType;
import com.da2jobu.deliverymanagerservice.domain.model.vo.HubId;
import com.da2jobu.deliverymanagerservice.domain.model.vo.UserId;
import com.da2jobu.deliverymanagerservice.domain.repository.DeliveryManagerRepository;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public class DeliveryManagerRepositoryAdapter implements DeliveryManagerRepository {

    private final JpaDeliveryManagerRepository jpaDeliveryManagerRepository;
    private final JPAQueryFactory queryFactory;

    @Override
    public DeliveryManager save(DeliveryManager deliveryManager) {
        return jpaDeliveryManagerRepository.save(deliveryManager);
    }

    @Override
    public Optional<DeliveryManager> findById(DeliveryManagerId deliveryManagerId) {
        return jpaDeliveryManagerRepository.findByDeliveryManagerIdAndDeletedAtIsNull(deliveryManagerId);
    }

    @Override
    public boolean existsByUserId(UserId userId) {
        return jpaDeliveryManagerRepository.existsByUserId_UserId(userId.getUserId());
    }

    @Override
    public long countActiveByTypeAndNullHub(DeliveryManagerType type) {
        return jpaDeliveryManagerRepository.findByTypeAndHubId_HubIdIsNullAndDeletedAtIsNull(type).size();
    }

    @Override
    public long countActiveByTypeAndHub(DeliveryManagerType type, HubId hubId) {
        return jpaDeliveryManagerRepository.findByTypeAndHubId_HubIdAndDeletedAtIsNull(type, hubId.getHubId()).size();
    }

    @Override
    public int findMaxSeqByTypeAndNullHubForUpdate(DeliveryManagerType type) {
        return jpaDeliveryManagerRepository
                .findTopByTypeAndHubId_HubIdIsNullAndDeletedAtIsNullOrderBySeqDesc(type)
                .map(DeliveryManager::getSeq)
                .orElse(0);
    }

    @Override
    public int findMaxSeqByTypeAndHubForUpdate(DeliveryManagerType type, HubId hubId) {
        return jpaDeliveryManagerRepository
                .findTopByTypeAndHubId_HubIdAndDeletedAtIsNullOrderBySeqDesc(type, hubId.getHubId())
                .map(DeliveryManager::getSeq)
                .orElse(0);
    }

    @Override
    public Page<DeliveryManager> search(DeliveryManagerType type, UUID hubId, UUID userId,String userRole , Pageable pageable) {
        QDeliveryManager qDeliveryManager = QDeliveryManager.deliveryManager;
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(qDeliveryManager.deletedAt.isNull());

        if (type != null) {
            builder.and(qDeliveryManager.type.eq(type));
        }
        if (hubId != null) {
            builder.and(qDeliveryManager.hubId.hubId.eq(hubId));
        }
        if ("DELIVERY_MANAGER".equals(userRole)) {
            builder.and(qDeliveryManager.userId.userId.eq(userId));
        }

        List<DeliveryManager> content = queryFactory
                .selectFrom(qDeliveryManager)
                .where(builder)
                .orderBy(qDeliveryManager.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(qDeliveryManager.count())
                .from(qDeliveryManager)
                .where(builder)
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0L : total);
    }
}