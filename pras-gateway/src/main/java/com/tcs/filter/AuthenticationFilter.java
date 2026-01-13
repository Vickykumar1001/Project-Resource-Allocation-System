package com.tcs.filter;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.tcs.util.ErrorUtils;
import com.tcs.util.JwtUtil;

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    @Autowired
    private RouteValidator validator;

    @Autowired
    private JwtUtil jwtUtil;

    public AuthenticationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return ((exchange, chain) -> {
            if (validator.isSecured.test(exchange.getRequest())) {
                // header contains token or not
                if (!exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                    return ErrorUtils.writeErrorResponse(exchange,
                            HttpStatus.UNAUTHORIZED,
                            "Unauthorized",
                            "Missing Authorization header",
                            null);
                }

                String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
                String token = null;
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    token = authHeader.substring(7);
                }
                if (token == null || token.trim().isEmpty()) {
                    return ErrorUtils.writeErrorResponse(exchange,
                            HttpStatus.UNAUTHORIZED,
                            "Unauthorized",
                            "Invalid Authorization header",
                            null);
                }

                try {
                    // validate token
                    jwtUtil.validateToken(token);

                } catch (Exception e) {
                    return ErrorUtils.writeErrorResponse(exchange,
                            HttpStatus.UNAUTHORIZED,
                            "Unauthorized",
                            "Invalid or expired token",
                            List.of(e.getMessage() != null ? e.getMessage() : "Token validation failed"));
                }
            }
            return chain.filter(exchange);
        });
    }

    public static class Config {
    }
}
