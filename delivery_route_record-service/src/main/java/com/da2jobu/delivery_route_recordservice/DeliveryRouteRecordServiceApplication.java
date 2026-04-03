package com.da2jobu.delivery_route_recordservice;

import common.config.JpaAuditingConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication(scanBasePackages = {
        "com.da2jobu.delivery_route_recordservice",
        "common"
})
@Import(JpaAuditingConfig.class)
public class DeliveryRouteRecordServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DeliveryRouteRecordServiceApplication.class, args);
    }

}
