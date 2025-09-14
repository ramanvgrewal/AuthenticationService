package com.grewal.user_management.jwt;

import com.grewal.user_management.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    @Override
    protected void doFilterInternal
            (HttpServletRequest request,
             HttpServletResponse response,
             FilterChain filterChain) throws ServletException, IOException {

        Long userId = null;
        String jwtToken = null;

        final String authHeader = request.getHeader("Authorization");

        if(authHeader != null && authHeader.startsWith("Bearer ")) {
            jwtToken = authHeader.substring(7);
        }
        if(jwtToken == null) {
            Cookie[]  cookies = request.getCookies();
            if(cookies != null) {
                for (Cookie cookie : cookies) {
                    if("JWT".equals(cookie.getName())) {
                        jwtToken = cookie.getValue();
                        break;
                    }
                }
            }
        }
        if(jwtToken == null) {
            filterChain.doFilter(request, response);
            return;
        }
        //Extract the userId from jwtToken
        userId = jwtService.extractUserId(jwtToken);

        if(userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            //fetch user details
            var userDetails =  userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if(jwtService.isTokenValid(jwtToken, userDetails)) {
                var authorities = userDetails.getAuthorities();

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, authorities);

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        filterChain.doFilter(request, response);
    }
}
