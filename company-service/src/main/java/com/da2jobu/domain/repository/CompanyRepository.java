package com.da2jobu.domain.repository;

import com.da2jobu.domain.model.entity.Company;

import java.util.Optional;
import java.util.UUID;

public interface CompanyRepository {
    Company save(Company company);
    Optional<Company> findByIdAndDeletedAtIsNull(UUID companyId);
}