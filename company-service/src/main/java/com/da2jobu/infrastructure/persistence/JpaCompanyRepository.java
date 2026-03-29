package com.da2jobu.infrastructure.persistence;

import com.da2jobu.domain.model.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JpaCompanyRepository extends JpaRepository<Company, UUID> {

}