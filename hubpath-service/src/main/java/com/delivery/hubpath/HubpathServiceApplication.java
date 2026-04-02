package com.delivery.hubpath;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(exclude = { SecurityAutoConfiguration.class })
@EnableFeignClients
@ComponentScan(basePackages = {"com.delivery.hubpath", "common"})
public class HubpathServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(HubpathServiceApplication.class, args);
    }

}
