package com.da2jobu.deliverymanagerservice.domain.repository;

import com.da2jobu.deliverymanagerservice.domain.model.entity.DeliveryManager;
import com.da2jobu.deliverymanagerservice.domain.model.vo.DeliveryManagerId;
import com.da2jobu.deliverymanagerservice.domain.model.vo.DeliveryManagerType;
import com.da2jobu.deliverymanagerservice.domain.model.vo.HubId;
import com.da2jobu.deliverymanagerservice.domain.model.vo.UserId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface DeliveryManagerRepository {

    DeliveryManager save(DeliveryManager deliveryManager);

    Optional<DeliveryManager> findById(DeliveryManagerId deliveryManagerId);

    Page<DeliveryManager> search(DeliveryManagerType type, UUID hubId, UUID userId,String userRole, Pageable pageable);

    boolean existsByUserId(UserId userId);

    long countActiveByTypeAndNullHub(DeliveryManagerType type);

    long countActiveByTypeAndHub(DeliveryManagerType type, HubId hubId);

    int findMaxSeqByTypeAndNullHubForUpdate(DeliveryManagerType type);

    int findMaxSeqByTypeAndHubForUpdate(DeliveryManagerType type, HubId hubId);
}