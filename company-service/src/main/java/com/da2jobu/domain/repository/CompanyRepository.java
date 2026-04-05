package com.da2jobu.domain.repository;

import com.da2jobu.domain.model.entity.Company;
import com.da2jobu.domain.model.vo.CompanyType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CompanyRepository {
    Company save(Company company);
    Optional<Company> findByIdAndDeletedAtIsNull(UUID companyId);
    Page<Company> search(String name, CompanyType type, UUID hubId, Pageable pageable);
    List<Company> findAllByIdsAndDeletedAtIsNull(List<UUID> companyIds);
}