package com.calendarugr.api_gateway.filters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
public class PathPrefixStripFilter implements WebFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(PathPrefixStripFilter.class);
    private static final String PREFIX = "/calendarugr/v1";
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        
        // Si la ruta comienza con el prefijo
        if (path.startsWith(PREFIX)) {
            // Quitar el prefijo
            String newPath = path.substring(PREFIX.length());
            if (newPath.isEmpty()) {
                newPath = "/";
            }
            
            log.info("PathPrefixStripFilter: Transformando ruta {} -> {}", path, newPath);
            
            // Crear una nueva request preservando los query parameters
            ServerHttpRequest newRequest = request.mutate()
                .path(newPath) // Modificar solo el path
                .build();
            
            // Continuar con la nueva request
            return chain.filter(exchange.mutate().request(newRequest).build());
        }
        
        // Si no hay cambios, continuar normalmente
        return chain.filter(exchange);
    }
    
    @Override
    public int getOrder() {
        // Máxima prioridad para ejecutar antes que SecurityFilterChain
        return Ordered.HIGHEST_PRECEDENCE;
    }
}