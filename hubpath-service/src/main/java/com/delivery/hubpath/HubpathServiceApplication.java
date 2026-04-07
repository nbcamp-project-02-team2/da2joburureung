package com.delivery.hubpath;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableFeignClients
@EnableDiscoveryClient
@ComponentScan(basePackages = {"com.delivery.hubpath", "common"})
public class HubpathServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(HubpathServiceApplication.class, args);
    }

}
