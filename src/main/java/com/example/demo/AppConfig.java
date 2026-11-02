package com.example.demo;

import org.springframework.context.annotation.Bean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.annotation.RequestScope;

@Configuration
@EnableConfigurationProperties(AppProperties.class)
public class AppConfig {

    // Spring calls this factory method and manages the returned object.
    // The bean's default name is "greetingService" (the method name).
    // One actual instance per HTTP request, shared within that request.
    // @RequestScope supplies a proxy so singleton controllers can inject it.
    @Bean
    @RequestScope
    public GreetingService greetingService() {
        return new GreetingService("Hello");
    }
}
