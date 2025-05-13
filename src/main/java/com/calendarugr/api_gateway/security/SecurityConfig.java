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
                                //CORS CONF
                                .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                                
                                // Auth service
                                .pathMatchers("/auth/login", "/auth/refresh", "/user/register", "/user/activate", "academic-subscription/calendar/**").permitAll()
                                
                                // User service
                                .pathMatchers(HttpMethod.GET, "/user/nickname/**","/user/email/**").hasAnyRole("STUDENT", "TEACHER", "ADMIN")
                                .pathMatchers(HttpMethod.PUT, "/user/nickname/**").hasAnyRole("STUDENT", "TEACHER", "ADMIN")
                                .pathMatchers(HttpMethod.PUT, "/user/password/**").hasAnyRole("STUDENT", "TEACHER", "ADMIN")
                                .pathMatchers(HttpMethod.POST, "/user/activate").hasAnyRole("STUDENT", "TEACHER", "ADMIN")
                                .pathMatchers(HttpMethod.PUT, "/user/deactivate/**").hasAnyRole("STUDENT", "TEACHER", "ADMIN")
                                .pathMatchers(HttpMethod.PUT, "/user/role/**").hasAnyRole("TEACHER", "ADMIN")
                                .pathMatchers(HttpMethod.PUT, "/user/activate-notifications/**").hasAnyRole("STUDENT", "TEACHER", "ADMIN")
                                .pathMatchers(HttpMethod.PUT, "/user/deactivate-notifications/**").hasAnyRole("STUDENT", "TEACHER", "ADMIN")
                                .pathMatchers(HttpMethod.GET, "/user/email-list/**").hasAnyRole("STUDENT", "TEACHER", "ADMIN")

                                .pathMatchers(HttpMethod.GET, "/user/all").hasRole("ADMIN")
                                .pathMatchers(HttpMethod.POST, "/user/admin/register").hasRole("ADMIN")
                                .pathMatchers(HttpMethod.PUT, "/user/admin/update/**").hasRole("ADMIN")
                                .pathMatchers(HttpMethod.DELETE, "/user/admin/delete/**").hasRole("ADMIN")

                                .pathMatchers(HttpMethod.GET, "/user/roles/all").hasRole("ADMIN")
                                .pathMatchers(HttpMethod.POST, "/user/roles/create").hasRole("ADMIN")
                                .pathMatchers(HttpMethod.DELETE, "/user/roles/delete/**").hasRole("ADMIN")

                                // Sechedule consumer service ( We permit everybody to GET public information ...)
                                // Post endpoint in this service are meant to be used by other services
                                .pathMatchers(HttpMethod.GET, "/schedule-consumer/**").permitAll() // Allow all GET requests to schedule-consumer service
                                .pathMatchers(HttpMethod.POST, "/schedule-consumer/**").hasRole("ADMIN") // Only allow ADMIN to POST to schedule-consumer service
                                
                                // Mail service
                                .pathMatchers(HttpMethod.POST, "/email/send").hasRole("ADMIN") // Only allow ADMIN to send emails

                                // Academic subscription service
                                .pathMatchers(HttpMethod.POST, "/academic-subscription/subscription").hasAnyRole("STUDENT", "TEACHER", "ADMIN")
                                .pathMatchers(HttpMethod.POST, "/academic-subscription/subscription-batching").hasAnyRole("STUDENT", "TEACHER", "ADMIN")
                                .pathMatchers(HttpMethod.GET, "/academic-subscription/classes", "/academic-subscription/entire-calendar").hasAnyRole("STUDENT", "TEACHER", "ADMIN")
                                .pathMatchers(HttpMethod.GET, "/academic-subscription/ics", "/academic-subscription/sync-url").hasAnyRole("STUDENT", "TEACHER", "ADMIN")
                                .pathMatchers(HttpMethod.GET, "/academic-subscription/subscriptions").permitAll()
                                .pathMatchers(HttpMethod.DELETE, "/academic-subscription/subscription-grade").hasAnyRole("STUDENT", "TEACHER", "ADMIN")
                                .pathMatchers(HttpMethod.DELETE, "/academic-subscription/subscription").hasAnyRole("STUDENT", "TEACHER", "ADMIN")

                                .pathMatchers(HttpMethod.GET, "/academic-subscription/group-event").hasAnyRole(  "TEACHER", "ADMIN")
                                .pathMatchers(HttpMethod.POST, "/academic-subscription/group-event").hasAnyRole( "TEACHER", "ADMIN")
                                .pathMatchers(HttpMethod.DELETE, "/academic-subscription/group-event").hasAnyRole( "TEACHER", "ADMIN")

                                .pathMatchers(HttpMethod.GET, "/academic-subscription/faculty-group-event").hasRole( "ADMIN")
                                .pathMatchers(HttpMethod.POST, "/academic-subscription/faculty-event").hasRole( "ADMIN")
                                .pathMatchers(HttpMethod.DELETE, "/academic-subscription/faculty-event").hasRole( "ADMIN")
                                
                                // Any other request
                                .anyExchange().authenticated() // All other requests require authentication
                )
                .requestCache(cache -> cache
                        .requestCache(NoOpServerRequestCache.getInstance())); // Disable request caching, sessions are stateless

        return http.build();
    }
}
