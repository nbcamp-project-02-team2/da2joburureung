package com.delivery.hubpath.domain.repository;

import com.delivery.hubpath.domain.model.HubPath;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HubPathRepository extends JpaRepository<HubPath, Long> {
}
