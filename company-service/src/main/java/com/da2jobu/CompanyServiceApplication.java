package com.da2jobu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.scheduling.annotation.EnableScheduling;

import static org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO;

@SpringBootApplication(
        exclude = {SecurityAutoConfiguration.class, ManagementWebSecurityAutoConfiguration.class},
        scanBasePackages = {"com.da2jobu", "common"}
)
@EnableFeignClients
@EnableScheduling
@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO)
public class CompanyServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(CompanyServiceApplication.class, args);
    }
}
