package com.tcs.employee.client;

import com.tcs.employee.dto.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;


@FeignClient(name = "auth-service", configuration = com.tcs.employee.config.FeignConfig.class)
public interface AuthClient {

    @GetMapping("/auth/internal/users")
    List<UserDto> getUsersByIds(@RequestParam String ids);
    
    @GetMapping("/auth/user-from-token")
    public ResponseEntity<UserDto> getUserFromToken();
}
