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

        // 1. OMITIR FILTRO PARA EL ENDPOINT DE SALUD
        // Esto evita que si no hay token, el filtro cause ruido en la seguridad
        String path = request.getServletPath();
        if (path.equals("/api/contacts/public/health") || 
                path.equals("/api/contacts/auth/login-proxy")) {
                filterChain.doFilter(request, response);
                return;
        }
        
        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            
            try {
                if (jwtUtils.validateToken(token)) {
                    String username = jwtUtils.getUsernameFromToken(token);
                    String role = jwtUtils.getRoleFromToken(token);
                    
                    if (username != null && role != null) {
                        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(role);
                        List<SimpleGrantedAuthority> authorities = Collections.singletonList(authority);

                        UsernamePasswordAuthenticationToken auth = 
                            new UsernamePasswordAuthenticationToken(username, null, authorities);
                        
                        SecurityContextHolder.getContext().setAuthentication(auth);
                    }
                }
            } catch (Exception e) {
                // Si el token es inválido o expiró, simplemente no seteamos la autenticación
                // Spring Security se encargará de rechazarlo en las rutas protegidas
                logger.error("Error validando token JWT: " + e.getMessage());
            }
        }
        filterChain.doFilter(request, response);
    }

}