package com.manager.app.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity; // <--- FALTA ESTA
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;    // <--- FALTA ESTA
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // <--- ESTA ES LA CLAVE
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource())) 
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // 1. LA EXCEPCIÓN DEBE IR PRIMERO
                .requestMatchers("/api/contacts/public/health").permitAll()
                
                // 2. LUEGO PROTEGEMOS TODO LO DEMÁS BAJO /api/contacts/
                .requestMatchers("/api/contacts/**").authenticated()
                
                // 3. EL RESTO (POR SI ACASO)
                .anyRequest().permitAll()
            );

        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Leemos la variable de Render
        String origins = System.getenv("ALLOWED_ORIGINS");
        if (origins == null || origins.isEmpty()) {
            origins = "http://localhost:5173"; // Default para tu desarrollo local
        }

        // Convertimos la cadena separada por comas en una lista para Spring
        List<String> allowedOriginsList = Arrays.asList(origins.split(","));

        configuration.setAllowedOrigins(allowedOriginsList);
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true); // Necesario para enviar el Token JWT

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}