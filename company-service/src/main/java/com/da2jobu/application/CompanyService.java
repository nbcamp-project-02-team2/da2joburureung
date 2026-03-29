package com.da2jobu.application;

import com.da2jobu.application.dto.command.CreateCompanyCommand;
import com.da2jobu.application.dto.result.CompanyResult;
import com.da2jobu.application.service.HubClient;
import com.da2jobu.application.service.LocationClient;
import com.da2jobu.domain.model.entity.Company;
import com.da2jobu.domain.model.vo.CompanyId;
import com.da2jobu.domain.model.vo.HubId;
import com.da2jobu.domain.model.vo.Location;
import com.da2jobu.domain.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CompanyService {
    // ========== Application Services ==========
    private final LocationClient locationClient;
    private final HubClient hubClient;
    // ========== Domain ==========
    private final CompanyRepository companyRepository;


    @Transactional
    public CompanyResult create(CreateCompanyCommand command) {
        validateHubExists(command.hubId());
        Location location = locationClient.resolveLocation(command.address());

        Company company = Company.create(
                CompanyId.of(),
                HubId.of(command.hubId()),
                command.name(),
                command.type(),
                location
        );
        Company savedCompany = companyRepository.save(company);
        /*
          kafka 로 user쪽 연계로직 :  company id 업데이트
         */
        return CompanyResult.from(savedCompany);
    }

    private void validateHubExists(UUID hubId) {
        hubClient.validateHubExists(hubId);
    }

}