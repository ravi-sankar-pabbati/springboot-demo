package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class HelloControllerTest {

    // Standalone MVC tests do not start Spring, so dependencies are supplied manually.
    private final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(
            new HelloController(new GreetingService("Hello"), "Greeting Demo",
                    new AppProperties("Greeting Demo", 5)))
            .setControllerAdvice(new GlobalExceptionHandler()).build();

    @Test
    void helloReturnsHelloWorld() throws Exception {
        mockMvc.perform(get("/hello"))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello, World!"));
    }

    @Test
    void helloGreetsTheRequestedName() throws Exception {
        mockMvc.perform(get("/hello").param("name", "Ravi"))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello, Ravi!"));
    }

    @Test
    void helloGreetsTheNameFromJsonBody() throws Exception {
        mockMvc.perform(post("/hello")
                        .contentType("application/json")
                        .content("""
                                {"name":"Ravi"}
                                """))
                .andExpect(status().isOk())
                .andExpect(header().string("X-App-Name", "Greeting Demo"))
                .andExpect(content().string("Hello, Ravi!"));
    }

    @Test
    void helloRejectsNullOrMissingName() throws Exception {
        for (String body : new String[]{"{\"name\":null}", "{}"}) {
            mockMvc.perform(post("/hello")
                            .contentType("application/json")
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Validation failed"))
                    .andExpect(jsonPath("$.fieldErrors.name").value("Name is required"));
        }
    }

    @Test
    void notNullAllowsEmptyName() throws Exception {
        mockMvc.perform(post("/hello")
                        .contentType("application/json")
                        .content("{\"name\":\"\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello, !"));
    }

    @Test
    void helloReturnsCentralErrorForMalformedOrMissingBody() throws Exception {
        for (String body : new String[]{"{", ""}) {
            mockMvc.perform(post("/hello")
                            .contentType("application/json")
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message")
                            .value("Request body is missing or contains invalid JSON"))
                    .andExpect(jsonPath("$.fieldErrors").isEmpty());
        }
    }
}
