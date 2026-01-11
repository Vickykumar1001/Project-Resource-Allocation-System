package com.tcs.filter;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.util.function.Predicate;

@Component
public class RouteValidator {

    public Predicate<ServerHttpRequest> isSecured =
            request -> !request.getURI().getPath().startsWith("/auth");
}
