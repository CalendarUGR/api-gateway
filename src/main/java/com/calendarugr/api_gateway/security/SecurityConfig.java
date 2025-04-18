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
                                .pathMatchers("/auth/login", "/auth/refresh", "/user/register", "/user/activate").permitAll()
                                
                                // User service
                                .pathMatchers(HttpMethod.GET, "/user/nickname/**","/user/email/**").hasAnyRole("STUDENT", "TEACHER", "ADMIN")
                                .pathMatchers(HttpMethod.PUT, "/user/update-nickname/**").hasAnyRole("STUDENT", "TEACHER", "ADMIN")
                                .pathMatchers(HttpMethod.PUT, "/user/change-password/**").hasAnyRole("STUDENT", "TEACHER", "ADMIN")
                                .pathMatchers(HttpMethod.POST, "/user/activate").hasAnyRole("STUDENT", "TEACHER", "ADMIN")
                                .pathMatchers(HttpMethod.POST, "/user/deactivate/**").hasAnyRole("STUDENT", "TEACHER", "ADMIN")
                                .pathMatchers(HttpMethod.PUT, "/user/change-role/**").hasAnyRole("TEACHER", "ADMIN")
                                .pathMatchers(HttpMethod.PUT, "/user/activate-notifications/**").hasAnyRole("STUDENT", "TEACHER", "ADMIN")
                                .pathMatchers(HttpMethod.PUT, "/user/deactivate-notifications/**").hasAnyRole("STUDENT", "TEACHER", "ADMIN")
                                .pathMatchers(HttpMethod.GET, "/user/get-emails/**").hasAnyRole("STUDENT", "TEACHER", "ADMIN")

                                .pathMatchers(HttpMethod.GET, "/user/all").hasRole("ADMIN")
                                .pathMatchers(HttpMethod.POST, "/user/crear-admin").hasRole("ADMIN")
                                .pathMatchers(HttpMethod.PUT, "/user/actualizar-admin/**").hasRole("ADMIN")
                                .pathMatchers(HttpMethod.DELETE, "/user/borrar-admin/**").hasRole("ADMIN")

                                .pathMatchers(HttpMethod.GET, "/user/role-all").hasRole("ADMIN")
                                .pathMatchers(HttpMethod.POST, "/user/role-create").hasRole("ADMIN")
                                .pathMatchers(HttpMethod.DELETE, "/user/role-delete/**").hasRole("ADMIN")

                                // Sechedule consumer service ( We permit everybody to GET public information ...)
                                // Post endpoint in this service are meant to be used by other services
                                .pathMatchers(HttpMethod.GET, "/schedule-consumer/**").permitAll() // Allow all GET requests to schedule-consumer service

                                // Mail service
                                .pathMatchers(HttpMethod.POST, "/email/send").hasRole("ADMIN") // Only allow ADMIN to send emails

                                // Academic subscription service
                                .pathMatchers(HttpMethod.POST, "/academic-subscription/subscribe").hasAnyRole("STUDENT", "TEACHER", "ADMIN")
                                .pathMatchers(HttpMethod.POST, "/academic-subscription/subscribe-batching").hasAnyRole("STUDENT", "TEACHER", "ADMIN")
                                .pathMatchers(HttpMethod.GET, "/academic-subscription/classes", "/academic-subscription/entire-calendar").hasAnyRole("STUDENT", "TEACHER", "ADMIN")
                                .pathMatchers(HttpMethod.GET, "/academic-subscription/subscribe/download-ics", "/academic-subscription/subscribe/get-sync-url").hasAnyRole("STUDENT", "TEACHER", "ADMIN")
                                .pathMatchers(HttpMethod.DELETE, "/academic-subscription/remove-grade").hasAnyRole("STUDENT", "TEACHER", "ADMIN")
                                .pathMatchers(HttpMethod.DELETE, "/academic-subscription/remove-subscription").hasAnyRole("STUDENT", "TEACHER", "ADMIN")

                                .pathMatchers(HttpMethod.POST, "/academic-subscription/create-group-event").hasAnyRole( "TEACHER", "ADMIN")
                                .pathMatchers(HttpMethod.DELETE, "/academic-subscription/remove-group-event").hasAnyRole( "TEACHER", "ADMIN")

                                .pathMatchers(HttpMethod.POST, "/academic-subscription/create-faculty-event").hasAnyRole( "ADMIN")
                                .pathMatchers(HttpMethod.DELETE, "/academic-subscription/remove-faculty-event").hasAnyRole( "ADMIN")
                                
                                // Any other request must be authenticated
                                .anyExchange().authenticated()
                )
                .requestCache(cache -> cache
                        .requestCache(NoOpServerRequestCache.getInstance())); // Disable request caching, sessions are stateless

        return http.build();
    }
}