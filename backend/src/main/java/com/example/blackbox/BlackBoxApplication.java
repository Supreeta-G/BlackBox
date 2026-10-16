package com.example.blackbox;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class BlackBoxApplication {

    public static void main(String[] args) {
        SpringApplication.run(BlackBoxApplication.class, args);
    }
}
