package com.da2jobu.infrastructure.persistence;

import com.da2jobu.domain.model.entity.Company;
import com.da2jobu.domain.model.vo.CompanyId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface JpaCompanyRepository extends JpaRepository<Company, UUID> {
    Optional<Company> findByCompanyIdAndDeletedAtIsNull(CompanyId companyId);
}