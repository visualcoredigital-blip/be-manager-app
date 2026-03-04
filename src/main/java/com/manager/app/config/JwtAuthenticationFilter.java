package com.manager.app.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority; // IMPORTANTE
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections; // IMPORTANTE
import java.util.List;        // IMPORTANTE

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            
            if (jwtUtils.validateToken(token)) {
                String username = jwtUtils.getUsernameFromToken(token);
                
                // 1. Extraemos el rol del token usando el nuevo método en JwtUtils
                String role = jwtUtils.getRoleFromToken(token);
                
                if (username != null && role != null) {
                    // 2. Convertimos el String del rol (ej: "ROLE_ADMIN") en una autoridad
                    SimpleGrantedAuthority authority = new SimpleGrantedAuthority(role);
                    
                    // 3. Creamos la lista de autoridades
                    List<SimpleGrantedAuthority> authorities = Collections.singletonList(authority);

                    // 4. Creamos la autenticación incluyendo las autoridades (roles)
                    UsernamePasswordAuthenticationToken auth = 
                        new UsernamePasswordAuthenticationToken(username, null, authorities);
                    
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            }
        }
        filterChain.doFilter(request, response);
    }
}