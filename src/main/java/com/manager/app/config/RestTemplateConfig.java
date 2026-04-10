package com.manager.app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.client.SimpleClientHttpRequestFactory; 

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        
        // Aumentamos a 45 segundos para dar tiempo a que los microservicios "despierten"
        factory.setConnectTimeout(45000); 
        factory.setReadTimeout(45000);    
        
        return new RestTemplate(factory);
    }
}