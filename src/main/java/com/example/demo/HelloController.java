package com.example.demo;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    private final GreetingService greetingService;
    private final String appName;
    private final AppProperties appProperties;

    // This controller is a singleton. Spring injects a proxy that delegates
    // to the GreetingService instance belonging to the current HTTP request.
    // A single constructor does not need @Autowired.
    public HelloController(GreetingService greetingService,
                           @Value("${app.name}") String appName,
                           AppProperties appProperties) {
        this.greetingService = greetingService;
        this.appName = appName;
        this.appProperties = appProperties;
    }

    // @Value reads one property into a constructor argument.
    @GetMapping("/hello/config/value")
    public String configValue() {
        return appName;
    }

    // @ConfigurationProperties groups related properties into one typed object.
    @GetMapping("/hello/config/properties")
    public AppProperties configProperties() {
        return appProperties;
    }

    @GetMapping("/hello")
    public String hello(@RequestParam(defaultValue = "World") String name) {
        return greetingService.greet(name);
    }

    // Send JSON such as {"name":"Ravi"} in the HTTP request body.
    // Spring converts that JSON into a GreetingRequest before calling this method.
    @PostMapping("/hello")
    public ResponseEntity<String> helloFromBody(@Valid @RequestBody GreetingRequest request) {
        // ResponseEntity wraps the status (200), headers, and response body.
        return ResponseEntity.ok()
                .header("X-App-Name", appName)
                .body(greetingService.greet(request.name()));
    }

    // @NotNull rejects a missing name or JSON null; empty strings are allowed.
    // @Valid above triggers validation before the controller method executes.
    public record GreetingRequest(@NotNull(message = "Name is required") String name) {
    }

    @GetMapping("/hello/scope")
    public ScopeExample scope(@RequestParam(defaultValue = "World") String name) {
        // These two calls use the SAME instance within this request.
        // Refresh the URL: the next request gets a NEW instance ID.
        return new ScopeExample(greetingService.greet(name),
                greetingService.getInstanceId(), greetingService.getInstanceId());
    }

    public record ScopeExample(String greeting, String firstInstanceId, String secondInstanceId) {
    }

}
