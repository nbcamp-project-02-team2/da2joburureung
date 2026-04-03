package com.da2jobu.deliverymanagerservice.domain.repository;

import com.da2jobu.deliverymanagerservice.domain.model.entity.DeliveryManager;
import com.da2jobu.deliverymanagerservice.domain.model.vo.DeliveryManagerType;
import com.da2jobu.deliverymanagerservice.domain.model.vo.HubId;
import com.da2jobu.deliverymanagerservice.domain.model.vo.UserId;

public interface DeliveryManagerRepository {

    DeliveryManager save(DeliveryManager deliveryManager);

    boolean existsByUserId(UserId userId);

    long countActiveByTypeAndNullHub(DeliveryManagerType type);

    long countActiveByTypeAndHub(DeliveryManagerType type, HubId hubId);

    int findMaxSeqByTypeAndNullHubForUpdate(DeliveryManagerType type);

    int findMaxSeqByTypeAndHubForUpdate(DeliveryManagerType type, HubId hubId);
}