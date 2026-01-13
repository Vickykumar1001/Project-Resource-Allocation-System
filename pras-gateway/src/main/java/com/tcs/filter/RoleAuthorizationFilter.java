package com.tcs.filter;

import com.tcs.util.ErrorUtils;
import com.tcs.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RoleAuthorizationFilter extends AbstractGatewayFilterFactory<RoleAuthorizationFilter.Config> {

    @Autowired
    private RouteValidator validator;

    @Autowired
    private JwtUtil jwtUtil;

    public RoleAuthorizationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            // If route is not secured according to route validator, skip role check
            if (!validator.isSecured.test(exchange.getRequest())) {
                return chain.filter(exchange);
            }

            // Retrieve token
            if (!exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                return ErrorUtils.writeErrorResponse(exchange,
                        HttpStatus.UNAUTHORIZED,
                        "Unauthorized",
                        "Missing Authorization header",
                        null);
            }

            String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ErrorUtils.writeErrorResponse(exchange,
                        HttpStatus.UNAUTHORIZED,
                        "Unauthorized",
                        "Invalid Authorization header",
                        null);
            }
            String token = authHeader.substring(7);

            try {
                jwtUtil.validateToken(token);
            } catch (Exception e) {
                return ErrorUtils.writeErrorResponse(exchange,
                        HttpStatus.UNAUTHORIZED,
                        "Unauthorized",
                        "Invalid or expired token",
                        List.of(e.getMessage() != null ? e.getMessage() : "Token validation failed"));
            }

            String role = null;
            try {
                role = jwtUtil.getRoleFromToken(token);
            } catch (Exception e) {
                // malformed claims
                return ErrorUtils.writeErrorResponse(exchange,
                        HttpStatus.FORBIDDEN,
                        "Forbidden",
                        "Unable to extract role from token",
                        List.of(e.getMessage() != null ? e.getMessage() : "Role claim missing or invalid"));
            }

            if (role == null) {
                return ErrorUtils.writeErrorResponse(exchange,
                        HttpStatus.FORBIDDEN,
                        "Forbidden",
                        "Role claim not present in token",
                        null);
            }

            String path = exchange.getRequest().getURI().getPath();

            if (isAllowed(role, path)) {
                return chain.filter(exchange);
            } else {
                return ErrorUtils.writeErrorResponse(exchange,
                        HttpStatus.FORBIDDEN,
                        "Forbidden",
                        String.format("Role '%s' is not allowed to access %s", role, path),
                        null);
            }
        };
    }

    private boolean isAllowed(String role, String path) {
        // always allow access to auth endpoints
        if (path.startsWith("/auth/")  || path.equals("/auth")) {
            return true;
        }

        switch (role.toUpperCase()) {
            case "ADMIN":
                return true;

            case "EMPLOYEE":
                return path.startsWith("/employees/") || path.equals("/employees");

            case "MANAGER":
                return path.startsWith("/employees/") || path.startsWith("/project/") ||
                       path.equals("/employees") || path.equals("/project");

            case "RMG":
                return path.startsWith("/employees/") || path.startsWith("/project/") || path.startsWith("/allocation/") ||
                       path.equals("/employees") || path.equals("/project") || path.equals("/allocation");

            default:
                return false;
        }
    }

    public static class Config {
    }
}
