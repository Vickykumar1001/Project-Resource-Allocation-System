package com.tcs.auth.config;

import java.io.IOException;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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
import tools.jackson.databind.ObjectMapper;

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

		if (header == null || !header.startsWith("Bearer ")) {
			chain.doFilter(req, res);
			return;
		}

		token = header.substring(7);

		try {
			userEmail = jwtService.extractUsername(token);

			if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
				UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

				if (jwtService.isTokenValid(token, userDetails)) {
					UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails,
							null, userDetails.getAuthorities());
					SecurityContextHolder.getContext().setAuthentication(authToken);
				}
			}
		} catch (Exception e) {
			sendUnauthorized(res, req, e.getMessage());
		}

		chain.doFilter(req, res);
	}

	private void sendUnauthorized(HttpServletResponse res, HttpServletRequest req, String message) throws IOException {

		res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		res.setContentType(MediaType.APPLICATION_JSON_VALUE);

		var body = new java.util.HashMap<String, Object>();
		body.put("timestamp", System.currentTimeMillis());
		body.put("status", 401);
		body.put("error", "Unauthorized");
		body.put("message", message != null ? message : "Invalid or expired token");
		body.put("path", req.getRequestURI());

		new ObjectMapper().writeValue(res.getWriter(), body);
	}
}