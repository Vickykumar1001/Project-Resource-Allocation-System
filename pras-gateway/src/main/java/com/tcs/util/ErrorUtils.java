package com.tcs.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tcs.dto.ErrorResponse;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;

public final class ErrorUtils {

    private static final ObjectMapper mapper = new ObjectMapper();

    private ErrorUtils() {}

    public static Mono<Void> writeErrorResponse(ServerWebExchange exchange,
                                                HttpStatus status,
                                                String error,
                                                String message,
                                                List<String> details) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        ErrorResponse err = new ErrorResponse();
        err.setTimestamp(System.currentTimeMillis());
        err.setStatus(status.value());
        err.setError(error);
        err.setMessage(message);
        err.setPath(exchange.getRequest().getURI().getPath());
        err.setErrors(details);

        byte[] bytes;
        try {
            bytes = mapper.writeValueAsBytes(err);
        } catch (Exception e) {
            String fallback = "{\"timestamp\": " + System.currentTimeMillis() +
                    ", \"status\": " + status.value() +
                    ", \"error\": \"" + escape(error) + "\"" +
                    ", \"message\": \"" + escape(message) + "\"" +
                    ", \"path\": \"" + escape(exchange.getRequest().getURI().getPath()) + "\"}";
            bytes = fallback.getBytes(StandardCharsets.UTF_8);
        }

        DataBufferFactory bufferFactory = response.bufferFactory();
        DataBuffer buffer = bufferFactory.wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }

    private static String escape(String s) {
        return s == null ? "" : s.replace("\"", "\\\"");
    }
}
