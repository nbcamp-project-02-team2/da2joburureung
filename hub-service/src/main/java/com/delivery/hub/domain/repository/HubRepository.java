package com.delivery.hub.domain.repository;

import com.delivery.hub.application.dto.SearchHubCommand;
import com.delivery.hub.domain.model.Hub;
import com.delivery.hub.interfaces.dto.Respone.HubResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface HubRepository extends JpaRepository<Hub, UUID>, HubRepositoryCustom {
}
