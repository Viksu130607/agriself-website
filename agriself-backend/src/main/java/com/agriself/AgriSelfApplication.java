package com.agriself;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
@EnableScheduling
public class AgriSelfApplication {

    public static void main(String[] args) {
        SpringApplication.run(AgriSelfApplication.class, args);
        System.out.println("==================================================================");
        System.out.println(" 🌱 AgriSelf Enterprise Java Platform (स्वायत्त खेती, बुद्धिमान विकास) Online! 🌱 ");
        System.out.println(" 🚜 Active Web Portal: http://localhost:8080");
        System.out.println(" 💾 H2 Database Console: http://localhost:8080/h2-console");
        System.out.println(" 🔒 Spring Security 6 + JWT RBAC Active");
        System.out.println("==================================================================");
    }
}
