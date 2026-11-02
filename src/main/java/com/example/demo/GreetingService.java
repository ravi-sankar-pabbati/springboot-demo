package com.example.demo;

import java.util.UUID;

// A plain Java class. AppConfig registers an instance as a Spring bean.
// No @Service or @Component is needed because we are using @Bean instead.
public class GreetingService {

    private final String greeting;
    private final String instanceId = UUID.randomUUID().toString();

    public GreetingService(String greeting) {
        this.greeting = greeting;
    }

    public String greet(String name) {
        return greeting + ", " + name + "!";
    }

    public String getInstanceId() {
        return instanceId;
    }
}
