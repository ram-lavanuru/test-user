package com.learner.LearnerUser;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class LearnerUserApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(LearnerUserApiApplication.class, args);
    }

}
