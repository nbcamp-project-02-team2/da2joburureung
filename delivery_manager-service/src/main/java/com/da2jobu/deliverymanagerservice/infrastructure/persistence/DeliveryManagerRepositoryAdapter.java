package com.da2jobu.deliverymanagerservice.infrastructure.persistence;

import com.da2jobu.deliverymanagerservice.domain.model.entity.DeliveryManager;
import com.da2jobu.deliverymanagerservice.domain.model.vo.DeliveryManagerType;
import com.da2jobu.deliverymanagerservice.domain.model.vo.HubId;
import com.da2jobu.deliverymanagerservice.domain.model.vo.UserId;
import com.da2jobu.deliverymanagerservice.domain.repository.DeliveryManagerRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DeliveryManagerRepositoryAdapter implements DeliveryManagerRepository {

    private final JpaDeliveryManagerRepository jpaDeliveryManagerRepository;

    @Override
    public DeliveryManager save(DeliveryManager deliveryManager) {
        return jpaDeliveryManagerRepository.save(deliveryManager);
    }

    @Override
    public boolean existsByUserId(UserId userId) {
        return jpaDeliveryManagerRepository.existsByUserId_UserId(userId.getUserId());
    }

    @Override
    public long countActiveByTypeAndNullHub(DeliveryManagerType type) {
        return jpaDeliveryManagerRepository.countByTypeAndHubId_HubIdIsNullAndDeletedAtIsNull(type);
    }

    @Override
    public long countActiveByTypeAndHub(DeliveryManagerType type, HubId hubId) {
        return jpaDeliveryManagerRepository.countByTypeAndHubId_HubIdAndDeletedAtIsNull(type, hubId.getHubId());
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
}