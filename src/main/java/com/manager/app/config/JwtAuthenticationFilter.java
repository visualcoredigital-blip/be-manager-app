package com.manager.app.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 1. OBTENER EL ENCABEZADO
        String header = request.getHeader("Authorization");

        // 2. SALIDA TEMPRANA SI NO HAY TOKEN
        // Si no hay token o no empieza con Bearer, dejamos que la petición siga.
        // Las rutas permitidas (como /health) pasarán, las protegidas darán 403 después.
        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. PROCESAR TOKEN (Solo si llegamos aquí es porque existe el header)
        String token = header.substring(7);
        
        try {
            if (jwtUtils.validateToken(token)) {
                String username = jwtUtils.getUsernameFromToken(token);
                String role = jwtUtils.getRoleFromToken(token);
                
                if (username != null && role != null) {
                    // Aseguramos el prefijo ROLE_ para que coincida con .hasRole() en SecurityConfig
                    String formattedRole = role.startsWith("ROLE_") ? role : "ROLE_" + role;
                    
                    SimpleGrantedAuthority authority = new SimpleGrantedAuthority(formattedRole);
                    List<SimpleGrantedAuthority> authorities = Collections.singletonList(authority);

                    UsernamePasswordAuthenticationToken auth = 
                        new UsernamePasswordAuthenticationToken(username, null, authorities);
                    
                    // Añadir detalles de la petición a la autenticación
                    auth.setDetails(new org.springframework.security.web.authentication.WebAuthenticationDetailsSource().buildDetails(request));
                    
                    SecurityContextHolder.getContext().setAuthentication(auth);
                    logger.info("✅ Token validado en Manager para usuario: {}", username);
                }
            }
        } catch (Exception e) {
            logger.error("❌ Error validando token JWT en Manager: {}", e.getMessage());
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}