package com.delivery.hub.domain.repository;

import com.delivery.hub.domain.model.Hub;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface HubRepository extends JpaRepository<Hub, UUID>, HubRepositoryCustom {

    @Query("SELECT COUNT(h) > 0 FROM Hub h WHERE h.hubName = :hubName")
    boolean existsByHubName(@Param("hubName") String hubName);

    List<Hub> findAllByDeletedAtIsNull();
}
