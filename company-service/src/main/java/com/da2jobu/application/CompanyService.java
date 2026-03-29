package com.da2jobu.application;

import com.da2jobu.presentation.dto.request.CreateCompanyRequest;
import com.da2jobu.presentation.dto.response.CompanyResponse;
import com.da2jobu.domain.model.entity.Company;
import com.da2jobu.domain.model.vo.Location;
import com.da2jobu.application.service.HubClient;
import com.da2jobu.domain.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final HubClient hubValidator;

    @Transactional
    public CompanyResponse create(CreateCompanyRequest request) {
        validateHubExists(request.hubId());

        Location location = Location.of(request.address(), request.latitude(), request.longitude());

        Company company = Company.create(
                request.managerId(),
                request.hubId(),
                request.name(),
                request.type(),
                location
        );

        return CompanyResponse.from(companyRepository.save(company));
    }

    private void validateHubExists(UUID hubId) {
        hubValidator.validateExists(hubId);
    }
}