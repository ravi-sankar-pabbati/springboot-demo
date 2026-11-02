package com.example.springbootdemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@org.springframework.boot.persistence.autoconfigure.EntityScan("com.example.demo.todo")
@org.springframework.data.jpa.repository.config.EnableJpaRepositories("com.example.demo.todo")
@SpringBootApplication(scanBasePackages = "com.example")
public class SpringbootDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringbootDemoApplication.class, args);
    }

}
