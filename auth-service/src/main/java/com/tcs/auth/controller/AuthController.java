package com.tcs.auth.controller;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.tcs.auth.dto.LoginRequest;
import com.tcs.auth.dto.LoginResponse;
import com.tcs.auth.dto.RegisterRequest;
import com.tcs.auth.dto.TokenValidationResponse;
import com.tcs.auth.dto.UserDto;
import com.tcs.auth.service.AuthServiceImpl;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AuthController {

	private final AuthServiceImpl authService;

	@PostMapping("/auth/register")
	public ResponseEntity<UserDto> register(@Valid @RequestBody RegisterRequest req) {
		var dto = authService.register(req);
		return ResponseEntity.status(HttpStatus.CREATED).body(dto);
	}

	@PostMapping("/auth/login")
	public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
		var resp = authService.login(req);
		return ResponseEntity.ok(resp);
	}

	@GetMapping("/auth/user-from-token")
	public ResponseEntity<UserDto> getUserFromToken() {
		ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		HttpServletRequest request = attrs.getRequest();
		String auth = request.getHeader("Authorization");
		if (auth == null || !auth.startsWith("Bearer ")) {
			throw new com.tcs.auth.exception.BadRequestException("Authorization header missing or malformed");
		}
		UserDto dto = authService.getUserFromToken(auth.substring(7));
		return ResponseEntity.ok(dto);
	}

	@PostMapping("/auth/validate")
	public ResponseEntity<TokenValidationResponse> validateToken(
			@RequestHeader(value = "Authorization", required = false) String authHeader) {

		TokenValidationResponse response = authService.validate(authHeader);
		return ResponseEntity.ok(response);
	}

	@GetMapping("/auth/internal/users")
	public ResponseEntity<?> internalUsers(@RequestParam(required = false) String ids,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {

		if (ids != null && !ids.isBlank()) {
			List<Long> parsed = Arrays.stream(ids.split(",")).map(String::trim).filter(s -> !s.isEmpty())
					.map(Long::valueOf).collect(Collectors.toList());
			List<UserDto> users = authService.getUsersByIds(parsed);
			return ResponseEntity.ok(users);
		} else {
			Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
			Page<UserDto> p = authService.getUsersPaginated(pageable);
			return ResponseEntity.ok(p);
		}
	}
	
	//soft delete
	@DeleteMapping("/auth/internal/users/{uid}")
	public ResponseEntity<Void> softDeleteUser(@PathVariable("uid") Long uid) {
		authService.softDeleteUser(uid);
		return ResponseEntity.noContent().build();
	}
}
