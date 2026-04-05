package com.da2jobu.deliveryservice.infrastructure.persistence;

import com.da2jobu.deliveryservice.domain.deliveryManager.model.entity.DeliveryAssignment;
import com.da2jobu.deliveryservice.domain.deliveryManager.model.entity.QDeliveryAssignment;
import com.da2jobu.deliveryservice.domain.deliveryManager.model.vo.DeliveryAssignmentId;
import com.da2jobu.deliveryservice.domain.deliveryManager.model.vo.DeliveryAssignmentStatus;
import com.da2jobu.deliveryservice.domain.deliveryManager.model.vo.DeliveryManagerId;
import com.da2jobu.deliveryservice.domain.deliveryManager.model.vo.ManagerIdleDuration;
import com.da2jobu.deliveryservice.domain.deliveryManager.repository.DeliveryAssignmentRepository;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@RequiredArgsConstructor
public class DeliveryAssignmentRepositoryAdapter implements DeliveryAssignmentRepository {
    private final JpaDeliveryAssignmentRepository jpaDeliveryAssignmentRepository;
    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<DeliveryAssignment> findById(DeliveryAssignmentId deliveryAssignmentId){
        return jpaDeliveryAssignmentRepository.findByDeliveryAssignmentIdAndDeletedAtIsNull(deliveryAssignmentId);
    }

    @Override
    public boolean hasActiveDelivery(DeliveryManagerId deliveryManagerId) {
        return jpaDeliveryAssignmentRepository.existsByDeliveryManagerIdAndStatusIn(
                deliveryManagerId,
                List.of(DeliveryAssignmentStatus.ASSIGNED)
        );
    }
    @Override
    public List<ManagerIdleDuration> findIdleDurationsByManagerIds(List<DeliveryManagerId> managerIds) {
        QDeliveryAssignment deliveryAssignment = QDeliveryAssignment.deliveryAssignment;
        LocalDateTime now = LocalDateTime.now();

        List<Tuple> results = queryFactory
                .select(deliveryAssignment.deliveryManagerId, deliveryAssignment.updatedAt.max())
                .from(deliveryAssignment)
                .where(
                        deliveryAssignment.deliveryManagerId.in(managerIds),
                        deliveryAssignment.status.in(DeliveryAssignmentStatus.COMPLETED)
                )
                .groupBy(deliveryAssignment.deliveryManagerId)
                .fetch();

        return results.stream().map(tuple -> new ManagerIdleDuration(
                tuple.get(deliveryAssignment.deliveryManagerId),
                Duration.between(Objects.requireNonNull(tuple.get(deliveryAssignment.updatedAt.max())), now)
        )).toList();
    }

    @Override
    public DeliveryAssignment save(DeliveryAssignment assignment) {
        return jpaDeliveryAssignmentRepository.save(assignment);
    }

    @Override
    public Page<DeliveryAssignment> findByManagerId(DeliveryManagerId deliveryManagerId,
                                                    DeliveryAssignmentStatus status,
                                                    Pageable pageable) {
        QDeliveryAssignment assignment = QDeliveryAssignment.deliveryAssignment;

        BooleanBuilder builder = new BooleanBuilder();
        builder.and(assignment.deliveryManagerId.eq(deliveryManagerId));
        builder.and(assignment.deletedAt.isNull());

        if (status != null) {
            builder.and(assignment.status.eq(status));
        }

        List<DeliveryAssignment> content = queryFactory
                .selectFrom(assignment)
                .where(builder)
                .orderBy(assignment.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(assignment.count())
                .from(assignment)
                .where(builder)
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0L : total);
    }
}