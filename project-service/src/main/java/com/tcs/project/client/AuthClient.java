package com.tcs.project.client;

import com.tcs.project.dto.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;


@FeignClient(name = "auth-service", configuration = com.tcs.project.config.FeignConfig.class)
public interface AuthClient {

    @GetMapping("/auth/internal/users")
    List<UserDto> getUsersByIds(@RequestParam String ids);
    
    @GetMapping("/auth/user-from-token")
    public ResponseEntity<UserDto> getUserFromToken();
    
}
