package org.sgai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAutoConfiguration
@EnableScheduling
public class SgaiYbttGatherApplication {
    public static void main(String[] args) {
        SpringApplication.run(SgaiYbttGatherApplication.class, args);
    }

}
