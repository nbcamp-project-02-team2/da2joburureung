package com.da2jobu.infrastructure.config;

import com.da2jobu.domain.service.CompanyDomainService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DomainServiceConfig {
    @Bean
    public CompanyDomainService companyDomainService() {
        return new CompanyDomainService();
    }
}
