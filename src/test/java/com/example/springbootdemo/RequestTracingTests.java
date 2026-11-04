package com.example.springbootdemo;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.example.demo.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "management.tracing.sampling.probability=1.0",
        "management.tracing.propagation.type=W3C",
        "management.tracing.export.otlp.enabled=false",
        "management.logging.export.otlp.enabled=false",
        "management.otlp.metrics.export.enabled=false"
})
@Import(RequestTracingTests.FailingController.class)
class RequestTracingTests {
    @LocalServerPort
    int port;

    private final HttpClient client = HttpClient.newHttpClient();

    @Test
    void independentRequestsHaveDifferentTraceIds() throws Exception {
        String first = traceId(get("/hello", null));
        String second = traceId(get("/hello", null));
        assertThat(first).matches("[0-9a-f]{32}");
        assertThat(second).matches("[0-9a-f]{32}").isNotEqualTo(first);
    }

    @Test
    void continuesValidW3cContext() throws Exception {
        String id = "0123456789abcdef0123456789abcdef";
        assertThat(traceId(get("/hello", "00-" + id + "-0123456789abcdef-01"))).isEqualTo(id);
    }

    @Test
    void invalidContextGeneratesNewTrace() throws Exception {
        assertThat(traceId(get("/hello", "invalid"))).matches("[0-9a-f]{32}");
    }

    @Test
    void clientErrorIncludesTraceId() throws Exception {
        HttpResponse<String> response = get("/api/todos/not-a-number", null);
        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(traceId(response)).matches("[0-9a-f]{32}");
    }

    @Test
    void serverErrorLogAndResponseShareTraceId() throws Exception {
        Logger logger = (Logger) LoggerFactory.getLogger(GlobalExceptionHandler.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>() {
            @Override
            protected void append(ILoggingEvent event) {
                event.prepareForDeferredProcessing();
                super.append(event);
            }
        };
        appender.start();
        logger.addAppender(appender);
        try {
            HttpResponse<String> response = get("/test/tracing-failure", null);
            assertThat(response.statusCode()).isEqualTo(500);
            assertThat(response.body()).doesNotContain("Internal failure details");
            String id = traceId(response);
            assertThat(id).matches("[0-9a-f]{32}");
            assertThat(appender.list).singleElement().satisfies(event -> {
                assertThat(event.getMDCPropertyMap()).containsEntry("traceId", id);
                assertThat(event.getThrowableProxy()).isNotNull();
            });
        }
        finally {
            logger.detachAppender(appender);
            appender.stop();
        }
    }

    private HttpResponse<String> get(String path, String traceparent) throws Exception {
        HttpRequest.Builder request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path));
        if (traceparent != null) {
            request.header("traceparent", traceparent);
        }
        return client.send(request.build(), HttpResponse.BodyHandlers.ofString());
    }

    private String traceId(HttpResponse<?> response) {
        return response.headers().firstValue("X-Trace-Id").orElseThrow();
    }

    @RestController
    static class FailingController {
        @GetMapping("/test/tracing-failure")
        String fail() {
            throw new IllegalStateException("Internal failure details");
        }
    }
}
