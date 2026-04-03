package com.da2jobu.deliverymanagerservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(
        exclude = {SecurityAutoConfiguration.class, ManagementWebSecurityAutoConfiguration.class},
        scanBasePackages = {"com.da2jobu", "common"}
)
@EnableFeignClients(basePackages = "com.da2jobu.deliverymanagerservice.infrastructure.client")
public class DeliveryManagerServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(DeliveryManagerServiceApplication.class, args);
    }
}
