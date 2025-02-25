package com.calendarugr.api_gateway.filters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.List;
import java.util.stream.Collectors;

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
                        String nickname = claims.getSubject();
                        Object rolesObject = claims.get("role");
    
                        List<String> roles;
                        if (rolesObject instanceof List) {
                            roles = (List<String>) rolesObject;
                        } else if (rolesObject instanceof String) {
                            roles = List.of((String) rolesObject);
                        } else {
                            roles = List.of(); // Si no hay roles, lista vacía
                        }
    
                        Authentication authentication = new UsernamePasswordAuthenticationToken(
                                nickname, null, roles.stream()
                                        .map(SimpleGrantedAuthority::new)
                                        .collect(Collectors.toList()));
    
                        logger.debug("Token validado, usuario autenticado: " + nickname);
    
                        // Establecer el contexto de seguridad
                        return chain.filter(exchange)
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
