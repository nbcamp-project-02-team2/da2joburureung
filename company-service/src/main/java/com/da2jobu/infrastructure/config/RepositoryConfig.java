package com.da2jobu.infrastructure.config;

import com.da2jobu.domain.repository.CompanyRepository;
import com.da2jobu.infrastructure.persistence.CompanyRepositoryAdapter;
import com.da2jobu.infrastructure.persistence.JpaCompanyRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RepositoryConfig {
    @Bean
    public CompanyRepository companyRepository(JpaCompanyRepository jpaCompanyRepository) {
        return new CompanyRepositoryAdapter(jpaCompanyRepository);
    }
}
