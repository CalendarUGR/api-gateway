package com.calendarugr.api_gateway.filters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.List;

@Component
public class JwtAuthenticationFilter implements WebFilter {

    private final String SECRET_KEY = System.getProperty("JWT_SECRET");
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private Mono<Claims> validateToken(String token) {
        return Mono.defer(() -> {
            try {
                Key key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
                Claims claims = Jwts.parserBuilder()
                        .setSigningKey(key)
                        .build()
                        .parseClaimsJws(token)
                        .getBody();
                logger.debug("Token validado: " + claims);
                return Mono.just(claims);
            } catch (ExpiredJwtException e) {
                logger.error("Token expirado");
                return Mono.error(new IllegalArgumentException("Token has expired"));
            } catch (JwtException e) {
                logger.error("Token inválido");
                return Mono.error(new IllegalArgumentException("Invalid token"));
            }
        });
    }

    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            return validateToken(token)
                    .flatMap(claims -> {
                        String id = claims.getSubject();
                        String role = claims.get("role", String.class);

                        logger.debug("Rol extraído del token: " + role);

                        Authentication authentication = new UsernamePasswordAuthenticationToken(
                                id, null, List.of(new SimpleGrantedAuthority(role)));

                        logger.debug("Token validado, usuario autenticado: " + id);

                        // Added headers
                        ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                                .header("X-User-ID", id)
                                .header("X-User-Role", role)
                                .build();

                        ServerWebExchange mutatedExchange = exchange.mutate().request(mutatedRequest).build();

                        // Security context
                        return chain.filter(mutatedExchange)
                                .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));
                    })
                    .onErrorResume(e -> {
                        logger.error("Error al validar el token: " + e.getMessage());
                        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                        return exchange.getResponse().setComplete();
                    });
        }

        return chain.filter(exchange);
    }
}