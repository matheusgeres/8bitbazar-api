package br.com.eightbitbazar.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "app.cors")
public record CorsProperties(
    List<String> allowedOrigins,
    List<String> allowedMethods,
    List<String> allowedHeaders,
    boolean allowCredentials,
    long maxAge
) {
    public CorsProperties {
        if (allowedOrigins == null) allowedOrigins = List.of("http://localhost:3000");
        if (allowedMethods == null) allowedMethods = List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS");
        if (allowedHeaders == null) allowedHeaders = List.of("*");
        if (maxAge == 0) maxAge = 3600;
    }
}
