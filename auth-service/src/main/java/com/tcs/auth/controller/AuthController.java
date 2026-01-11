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
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
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
import com.tcs.auth.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;

	@PostMapping("/api/auth/register")
	public ResponseEntity<UserDto> register(@Valid @RequestBody RegisterRequest req) {
		System.out.println("coming here");
		var dto = authService.register(req);
		return ResponseEntity.status(HttpStatus.CREATED).body(dto);
	}

	@PostMapping("/api/auth/login")
	public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
		var resp = authService.login(req);
		return ResponseEntity.ok(resp);
	}

	@GetMapping("/api/auth/me")
	public ResponseEntity<UserDto> me(Authentication authentication) {
		if (authentication == null || !authentication.isAuthenticated())
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		String email = ((org.springframework.security.core.userdetails.User) authentication.getPrincipal())
				.getUsername();
		UserDto dto = authService.getByEmail(email);
		return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
	}

	// internal endpoint: GET /api/internal/users?ids=1,2,3
	// or GET /api/internal/users?page=0&size=20
	@GetMapping("/api/internal/users")
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

	@GetMapping("/api/auth/user-from-token")
	public ResponseEntity<UserDto> getUserFromToken() {

		ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

		HttpServletRequest request = attrs.getRequest();
		String token = request.getHeader("Authorization");

		UserDto dto = authService.getUserFromToken(token.substring(7));
		return ResponseEntity.ok(dto);
	}
	 @PostMapping("api/auth/validate")
	    public ResponseEntity<TokenValidationResponse> validateToken(
	            @RequestHeader(value = "Authorization", required = false) String authHeader) {

	        TokenValidationResponse response = authService.validate(authHeader);

	        if (!response.isValid()) {
	            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
	        }

	        return ResponseEntity.ok(response);
	    }

}
