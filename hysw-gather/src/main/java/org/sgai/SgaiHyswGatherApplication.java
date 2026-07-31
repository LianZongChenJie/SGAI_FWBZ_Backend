package org.sgai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAutoConfiguration
@EnableScheduling
public class SgaiHyswGatherApplication {
    public static void main(String[] args) {
        SpringApplication.run(SgaiHyswGatherApplication.class, args);
    }
}