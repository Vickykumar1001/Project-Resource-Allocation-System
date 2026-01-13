package com.tcs.auth.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.tcs.auth.dto.LoginRequest;
import com.tcs.auth.dto.LoginResponse;
import com.tcs.auth.dto.RegisterRequest;
import com.tcs.auth.dto.TokenValidationResponse;
import com.tcs.auth.dto.UserDto;

public interface AuthService {

	UserDto register(RegisterRequest req);

	LoginResponse login(LoginRequest req);

	UserDto getByEmail(String email);

	Page<UserDto> getUsersPaginated(Pageable pageable);

	List<UserDto> getUsersByIds(List<Long> ids);

	UserDto getUserFromToken(String token);

	TokenValidationResponse validate(String authHeader);

	void softDeleteUser(Long userId);
}
