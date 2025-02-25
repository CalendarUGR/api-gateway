package com.calendarugr.api_gateway.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.savedrequest.NoOpServerRequestCache;

import com.calendarugr.api_gateway.filters.JwtAuthenticationFilter;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter();
    }

    @Bean
    SecurityWebFilterChain securityFilterChain(ServerHttpSecurity http) throws Exception {
        http
                .addFilterAt(jwtAuthenticationFilter(), SecurityWebFiltersOrder.AUTHENTICATION)
                .csrf(csrf -> csrf.disable())
                .authorizeExchange(exchange -> exchange
                                .pathMatchers("/auth/login", "/auth/refresh").permitAll()
                                .pathMatchers(HttpMethod.GET, "/user/**").permitAll()
                                .pathMatchers(HttpMethod.POST, "/user").authenticated()
                                .pathMatchers(HttpMethod.PUT, "/user/**").authenticated()
                                .pathMatchers(HttpMethod.DELETE, "/user/**").authenticated()
                                .anyExchange().denyAll()
                )
                .requestCache(cache -> cache
                        .requestCache(NoOpServerRequestCache.getInstance())); // Disable request caching, sessions are stateless

        return http.build();
    }
}