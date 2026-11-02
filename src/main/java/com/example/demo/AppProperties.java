package com.example.demo;

import org.springframework.boot.context.properties.ConfigurationProperties;

// Binds app.name to name and app.max-greetings to maxGreetings.
// Spring converts the configured text "5" to an int.
@ConfigurationProperties(prefix = "app")
public record AppProperties(String name, int maxGreetings) {
}
