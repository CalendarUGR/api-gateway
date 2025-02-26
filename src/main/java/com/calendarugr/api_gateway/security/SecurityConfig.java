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
                                // Auth service
                                .pathMatchers("/auth/login", "/auth/refresh").permitAll()
                                // User service
                                .pathMatchers(HttpMethod.GET, "/user/nickname/**","/user/email/**").hasAnyRole("STUDENT", "TEACHER", "ADMIN")
                                .pathMatchers(HttpMethod.GET, "/user/all").hasRole("ADMIN")

                                .pathMatchers(HttpMethod.POST, "/user").hasRole("ADMIN")
                                .pathMatchers(HttpMethod.PUT, "/user/**").hasRole("ADMIN")
                                .pathMatchers(HttpMethod.DELETE, "/user/**").hasRole("ADMIN")

                                .pathMatchers(HttpMethod.PUT, "/user/updateNickname/**").hasAnyRole("STUDENT", "TEACHER", "ADMIN")

                                // Any other request must be authenticated
                                .anyExchange().authenticated()
                )
                .requestCache(cache -> cache
                        .requestCache(NoOpServerRequestCache.getInstance())); // Disable request caching, sessions are stateless

        return http.build();
    }
}