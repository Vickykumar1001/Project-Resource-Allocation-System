package com.tcs.auth.config;

import java.io.IOException;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
//Remove @Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

 private final JwtService jwtService;
 private final UserDetailsService userDetailsService;
 @Override
 protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
         throws IOException, ServletException {

     final String header = req.getHeader(HttpHeaders.AUTHORIZATION);
     final String token;
     final String userEmail;

     // 1. If no token, just move on to the next filter (SecurityConfig will decide if access is allowed)
     if (header == null || !header.startsWith("Bearer ")) {
         chain.doFilter(req, res);
         return;
     }

     token = header.substring(7);
     
     try {
         userEmail = jwtService.extractUsername(token);

         if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
             UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);
             
             // 2. Check if token is valid for this user
             if (jwtService.isTokenValid(token, userDetails)) {
                 UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                         userDetails, null, userDetails.getAuthorities());
                 SecurityContextHolder.getContext().setAuthentication(authToken);
             }
         }
     } catch (Exception e) {
         // Log the error, but DO NOT throw it. 
         // Just let the request proceed. If it's a protected route, 
         // Spring Security will block it later automatically.
     }

     chain.doFilter(req, res);
 }
}