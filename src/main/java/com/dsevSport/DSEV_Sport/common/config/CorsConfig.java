package com.dsevSport.DSEV_Sport.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource(@Value("${APP_CORS_ALLOWED_ORIGINS:}") String allowedOrigins) {
        CorsConfiguration config = new CorsConfiguration();
        if (allowedOrigins == null || allowedOrigins.isBlank()) {
            config.setAllowedOriginPatterns(List.of("*")); // permissive fallback for deployed origin; use only if acceptable
        } else {
            config.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));
        }
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
