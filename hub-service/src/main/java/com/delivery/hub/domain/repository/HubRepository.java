package com.delivery.hub.domain.repository;

import com.delivery.hub.domain.model.Hub;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface HubRepository extends JpaRepository<Hub, UUID>, HubRepositoryCustom {

    @Query(value = "SELECT COUNT(*) FROM p_hub WHERE hub_name = :hubName AND deleted_at IS NULL", nativeQuery = true)
    int countActiveHubByName(@Param("hubName") String hubName);

    List<Hub> findAllByDeletedAtIsNull();

    Optional<Hub> findByHubIdAndDeletedAtIsNull(UUID hubId);
}
