package com.example.springbootdemo;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class SpringbootDemoApplicationTests {

    @Autowired
    private WebApplicationContext context;

    @Test
    void requestScopeReusesInstanceWithinRequestAndCreatesNewInstanceForNextRequest() throws Exception {
        // Use the real Spring context: standalone controllers cannot test bean scopes.
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        String first = mockMvc.perform(
                        get("/hello/scope").param("name", "Ravi"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String second = mockMvc.perform(get("/hello/scope"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        String firstId = JsonPath.read(first, "$.firstInstanceId");
        String repeatedId = JsonPath.read(first, "$.secondInstanceId");
        String nextRequestId = JsonPath.read(second, "$.firstInstanceId");
        assertEquals("Hello, Ravi!", JsonPath.<String>read(first, "$.greeting"));
        assertEquals(firstId, repeatedId);
        assertNotEquals(firstId, nextRequestId);
    }

}
