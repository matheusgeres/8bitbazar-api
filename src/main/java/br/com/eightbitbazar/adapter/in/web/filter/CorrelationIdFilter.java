package br.com.eightbitbazar.adapter.in.web.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Component
public class CorrelationIdFilter extends OncePerRequestFilter {

    private static final String CORRELATION_ID_HEADER = "X-Correlation-Id";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        Map<String, String> previousMdc = MDC.getCopyOfContextMap();
        try {
            String raw = request.getHeader(CORRELATION_ID_HEADER);
            String correlationId;
            if (raw == null || raw.isBlank()) {
                correlationId = UUID.randomUUID().toString();
            } else {
                // Strip control characters and enforce length
                String sanitized = raw.replaceAll("[\\r\\n]", "").strip();
                if (sanitized.isBlank() || sanitized.length() > 64) {
                    correlationId = UUID.randomUUID().toString();
                } else {
                    correlationId = sanitized;
                }
            }
            MDC.put("correlationId", correlationId);

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof Jwt jwt) {
                String userId = jwt.getSubject();
                if (userId != null) {
                    MDC.put("userId", userId);
                }
            }

            response.setHeader(CORRELATION_ID_HEADER, correlationId);
            chain.doFilter(request, response);
        } finally {
            if (previousMdc == null) {
                MDC.clear();
            } else {
                MDC.setContextMap(previousMdc);
            }
        }
    }
}
