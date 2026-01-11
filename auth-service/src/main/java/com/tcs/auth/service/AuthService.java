package com.tcs.auth.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.tcs.auth.config.JwtService;
import com.tcs.auth.dto.LoginRequest;
import com.tcs.auth.dto.LoginResponse;
import com.tcs.auth.dto.RegisterRequest;
import com.tcs.auth.dto.TokenValidationResponse;
import com.tcs.auth.dto.UserDto;
import com.tcs.auth.entity.User;
import com.tcs.auth.mapper.UserMapper;
import com.tcs.auth.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final CustomUserDetailsService customUserDetailsService;

    @Transactional
    public UserDto register(RegisterRequest req) {
        if (userRepository.existsByEmailAndDeletedFalse(req.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }
        String encoded = passwordEncoder.encode(req.getPassword());
        User user = UserMapper.fromRegister(req.getFullName(), req.getEmail(), encoded);
        user = userRepository.save(user);
        return UserMapper.toDto(user);
    }

    public LoginResponse login(LoginRequest req) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword())
        );
        User user = userRepository.findByEmailAndDeletedFalse(req.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));
        String token = jwtService.generateToken(user.getEmail(), user.getRole().name());
        return new LoginResponse(token, user.getId(), user.getEmail(), user.getFullName(), user.getRole().name());
    }

    public UserDto getByEmail(String email) {
        return userRepository.findByEmailAndDeletedFalse(email).map(UserMapper::toDto).orElse(null);
    }

    public Page<UserDto> getUsersPaginated(Pageable pageable) {
        Page<User> page = userRepository.findAllByDeletedFalse(pageable);
        return page.map(UserMapper::toDto);
    }

    public List<UserDto> getUsersByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return List.of();
        List<User> users = userRepository.findAllByIdInAndDeletedFalse(ids);
        return UserMapper.toDtoList(users);
    }
    
    public UserDto getUserFromToken(String token) {


        // 1. Extract email from token subject
        String email = jwtService.extractUsername(token);

        // 2. Load user from DB
        User user = userRepository.findByEmailAndDeletedFalse(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // 3. Map to DTO
        return UserMapper.toDto(user);
    }
    
    public TokenValidationResponse validate(String authHeader) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return new TokenValidationResponse(false, null);
        }

        String token = authHeader.substring(7);

        try {
            String username = jwtService.extractUsername(token);
            UserDetails userDetails =
            		customUserDetailsService.loadUserByUsername(username);

            boolean valid = jwtService.isTokenValid(token, userDetails);

            if (!valid) {
                return new TokenValidationResponse(false, null);
            }

            return new TokenValidationResponse(true, username);

        } catch (Exception e) {
            return new TokenValidationResponse(false, null);
        }
    }
}
