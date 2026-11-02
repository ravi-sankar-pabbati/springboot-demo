package com.example.springbootdemo;

import com.example.demo.todo.TodoRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
class TodoApiTests {
    @Autowired WebApplicationContext context;
    @Autowired TodoRepository repository;
    MockMvc mvc;

    @BeforeEach
    void setup() {
        repository.deleteAll();
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void crudPersistsChangesAndReturnsExpectedStatuses() throws Exception {
        String body = mvc.perform(post("/api/todos").contentType("application/json")
                        .content("{\"title\":\" Buy milk \",\"description\":\"Two bottles\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Buy milk"))
                .andExpect(jsonPath("$.completed").value(false))
                .andReturn().getResponse().getContentAsString();
        Number id = JsonPath.read(body, "$.id");
        String path = "/api/todos/" + id;
        mvc.perform(get(path)).andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Two bottles"));
        mvc.perform(put(path).contentType("application/json")
                        .content("{\"title\":\"Milk bought\",\"completed\":true}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.completed").value(true));
        mvc.perform(get(path)).andExpect(jsonPath("$.title").value("Milk bought"))
                .andExpect(jsonPath("$.completed").value(true));
        mvc.perform(get("/api/todos")).andExpect(jsonPath("$.length()").value(1));
        mvc.perform(delete(path)).andExpect(status().isNoContent());
        mvc.perform(get(path)).andExpect(status().isNotFound());
        mvc.perform(put(path).contentType("application/json").content("{\"title\":\"Missing\"}"))
                .andExpect(status().isNotFound());
        mvc.perform(delete(path)).andExpect(status().isNotFound());
        mvc.perform(get("/api/todos")).andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void dueDateIsPersistedUpdatedAndCleared() throws Exception {
        String body = mvc.perform(post("/api/todos").contentType("application/json")
                        .content("{\"title\":\"Buy groceries\",\"dueDate\":\"2026-11-05\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.dueDate").value("2026-11-05"))
                .andReturn().getResponse().getContentAsString();
        Number id = JsonPath.read(body, "$.id");
        String path = "/api/todos/" + id;
        mvc.perform(get(path)).andExpect(status().isOk())
                .andExpect(jsonPath("$.dueDate").value("2026-11-05"));
        mvc.perform(put(path).contentType("application/json")
                        .content("{\"title\":\"Buy groceries\",\"dueDate\":\"2026-11-06\"}"))
                .andExpect(status().isOk());
        mvc.perform(get(path)).andExpect(status().isOk())
                .andExpect(jsonPath("$.dueDate").value("2026-11-06"));
        mvc.perform(put(path).contentType("application/json")
                        .content("{\"title\":\"Buy groceries\"}"))
                .andExpect(status().isOk());
        mvc.perform(get(path)).andExpect(status().isOk())
                .andExpect(jsonPath("$.dueDate").doesNotExist());
    }

    @Test
    void rejectsMalformedDueDate() throws Exception {
        mvc.perform(post("/api/todos").contentType("application/json")
                        .content("{\"title\":\"Buy groceries\",\"dueDate\":\"not-a-date\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsInvalidRequests() throws Exception {
        for (String body : new String[]{"{}", "{\"title\":\"   \"}",
                "{\"title\":\"" + "x".repeat(201) + "\"}",
                "{\"title\":\"Valid\",\"description\":\"" + "x".repeat(2001) + "\"}"}) {
            mvc.perform(post("/api/todos").contentType("application/json").content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Validation failed"));
        }
        mvc.perform(post("/api/todos").contentType("application/json").content("{"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/todos/invalid")).andExpect(status().isBadRequest());
    }
}
