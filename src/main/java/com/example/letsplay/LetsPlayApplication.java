package com.example.letsplay;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

//@SpringBootApplication(exclude = ErrorMvcAutoConfiguration.class)
@SpringBootApplication
public class LetsPlayApplication {

    public static void main(String[] args) {
        SpringApplication.run(LetsPlayApplication.class, args);
    }

}
