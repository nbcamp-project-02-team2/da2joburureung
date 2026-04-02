package com.delivery.hubpath.domain.repository;

import com.delivery.hubpath.domain.model.HubPath;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface HubPathRepository extends JpaRepository<HubPath, UUID> {
    Page<HubPath> findByDepartHubNameContainingAndArriveHubNameContaining(String departName, String arriveName, Pageable pageable);
}
