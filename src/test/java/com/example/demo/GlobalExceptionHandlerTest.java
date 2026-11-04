package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTest {
    private final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new FailingController())
            .setControllerAdvice(new GlobalExceptionHandler()).build();

    @Test
    void unexpectedFailureReturnsGenericResponse() throws Exception {
        mockMvc.perform(get("/failure"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("An unexpected server error occurred"))
                .andExpect(jsonPath("$.fieldErrors").isEmpty());
    }

    @Test
    void unsupportedMethodPreservesStatusAndAllowHeader() throws Exception {
        mockMvc.perform(post("/failure"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(header().string("Allow", "GET"));
    }

    @Test
    void explicitHttpStatusIsPreserved() throws Exception {
        mockMvc.perform(get("/conflict"))
                .andExpect(status().isConflict());
    }

    @RestController
    static class FailingController {
        @GetMapping("/failure")
        String fail() {
            throw new IllegalStateException("Internal implementation details");
        }

        @GetMapping("/conflict")
        String conflict() {
            throw new ResponseStatusException(HttpStatus.CONFLICT);
        }
    }
}
