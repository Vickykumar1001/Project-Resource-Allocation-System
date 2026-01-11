package com.tcs.employee.client;

import com.tcs.employee.dto.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Feign client to call Auth service internal endpoints.
 * auth.service.url is configured in application.properties
 */
@FeignClient(name = "auth-service", configuration = com.tcs.employee.config.FeignConfig.class)
public interface AuthClient {

    // Call Auth service internal endpoint to fetch users by ids in batch
    // Example: /api/internal/users?ids=1,2,3
    @GetMapping("/api/internal/users")
    List<UserDto> getUsersByIds(@RequestParam(name = "ids") String ids);
    
    @GetMapping("/api/auth/user-from-token")
    public ResponseEntity<UserDto> getUserFromToken();
}
