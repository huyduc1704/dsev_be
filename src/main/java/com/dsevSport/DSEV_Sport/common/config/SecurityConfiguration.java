package com.dsevSport.DSEV_Sport.common.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(jsr250Enabled = true)
@RequiredArgsConstructor
public class SecurityConfiguration {
    private final AuthenticationProvider authenticationProvider;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, CorsConfigurationSource corsConfigurationSource) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // public: auth + swagger
                        .requestMatchers(
                                "/api/v1/auth/**",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()

                        // public: SePay webhook + VNPAY callback
                        .requestMatchers("/api/v1/sepay/webhook").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/payment/vnpay/callback").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/payment/vnpay/callback").permitAll()

                        // public: READ products & categories
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/products/**",
                                "/api/v1/categories/**",
                                "/api/v1/tags/**"
                        ).permitAll()

                        // WRITE products & categories: ADMIN or MODERATOR
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/products/**",
                                "/api/v1/categories/**",
                                "/api/v1/tags/**"
                        ).hasAnyRole("ADMIN", "MODERATOR")
                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/v1/products/**",
                                "/api/v1/categories/**",
                                "/api/v1/tags/**"
                        ).hasAnyRole("ADMIN", "MODERATOR")
                        .requestMatchers(
                                HttpMethod.PATCH,
                                "/api/v1/products/**",
                                "/api/v1/categories/**",
                                "/api/v1/tags/**"
                        ).hasAnyRole("ADMIN", "MODERATOR")
                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/v1/products/**",
                                "/api/v1/categories/**",
                                "/api/v1/tags/**"
                        ).hasAnyRole("ADMIN", "MODERATOR")

                        // user images: require authentication (token)
                        .requestMatchers("/api/v1/me/images/**").authenticated()

                        // các API /api/v1/me/** khác (profile, cart, orders, tryon, addresses, ...)
                        .requestMatchers("/api/v1/me/**").authenticated()
                        .requestMatchers("/api/v1/addresses/**").authenticated()
                        .requestMatchers("/api/v1/orders/**").authenticated()
                        .requestMatchers("/api/v1/tryon/**").authenticated()
                        .requestMatchers("/api/v1/payment/**").authenticated()

                        // mọi request khác: cần authentication
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Configuration
    @EnableMethodSecurity
    public class MethodSecurityConfig { }
}
