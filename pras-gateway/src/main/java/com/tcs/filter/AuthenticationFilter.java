package com.tcs.filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.tcs.util.JwtUtil;

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    @Autowired
    private RouteValidator validator;

        @Autowired
    private RestTemplate template;
    @Autowired
    private JwtUtil jwtUtil;

    public AuthenticationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return ((exchange, chain) -> {
            if (validator.isSecured.test(exchange.getRequest())) {
                //header contains token or not
                if (!exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                    throw new RuntimeException("missing authorization header");
                }

                String authHeader = exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    authHeader = authHeader.substring(7);
                }
                try {
//                    //REST call to AUTH service
//                    template.getForObject("http://AUTH-SERVICE/api/auth/validate?token" + authHeader, String.class);
                    jwtUtil.validateToken(authHeader);
                    System.out.println("headerrrrrrrrrrrrrrrrrrrrr: "+authHeader);

                } catch (Exception e) {
                    System.out.println("invalid access...!");
                    throw new RuntimeException("un authorized access to application");
                }
            }
            return chain.filter(exchange);
        });
    }

    public static class Config {

    }
}







//package com.tcs.filter;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.cloud.gateway.filter.GatewayFilter;
//import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.HttpStatus;
//import org.springframework.stereotype.Component;
//import org.springframework.web.reactive.function.client.WebClient;
//
//import com.tcs.dto.TokenValidationResponse;
//
//@Component
//public class AuthenticationFilter
//        extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {
//
//    @Autowired
//    private RouteValidator validator;
//
//    @Autowired
//    private WebClient.Builder webClientBuilder;
//
//    public AuthenticationFilter() {
//        super(Config.class);
//    }
//
//    @Override
//    public GatewayFilter apply(Config config) {
//
//        return (exchange, chain) -> {
//
//            if (validator.isSecured.test(exchange.getRequest())) {
//
//                String authHeader = exchange.getRequest()
//                        .getHeaders()
//                        .getFirst(HttpHeaders.AUTHORIZATION);
//
//                if (authHeader == null) {
//                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
//                    return exchange.getResponse().setComplete();
//                }
//
//                return webClientBuilder.build()
//                        .post()
//                        .uri("lb://AUTH-SERVICE/api/internal/auth/validate")
//                        .header(HttpHeaders.AUTHORIZATION, authHeader)
//                        .retrieve()
//                        .bodyToMono(TokenValidationResponse.class)
//                        .flatMap(resp -> {
//                            if (!resp.isValid()) {
//                                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
//                                return exchange.getResponse().setComplete();
//                            }
//                            return chain.filter(exchange);
//                        });
//            }
//
//            return chain.filter(exchange);
//        };
//    }
//
//    public static class Config {}
//}
