package com.example.letsplay.config;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.letsplay.exception.InvalidJwtTokenException;
import com.mongodb.lang.NonNull;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import io.jsonwebtoken.ExpiredJwtException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
        @NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        try {
            final String authorizationHeader = request.getHeader("Authorization");
            final String jtw;
            final String username;
            if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
                filterChain.doFilter(request, response);
                return;
            }
            jtw = authorizationHeader.substring(7);
            username = jwtService.extractUsername(jtw);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // User not connected yet
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
                if (jwtService.isTokenValid(jtw, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                    );
                    authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                    );
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            } else {
                throw new InvalidJwtTokenException();
            }
            filterChain.doFilter(request, response);
        } catch (ExpiredJwtException ex) {
            if (!request.getRequestURI().equals("/products/")) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\": \"JWT token has expired.\"}");
                response.getWriter().flush();
            } else {
                filterChain.doFilter(request, response);
                return;
            }
        }
    }
}

