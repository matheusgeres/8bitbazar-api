package br.com.eightbitbazar.adapter.in.web.filter;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CorrelationIdFilterTest {

    private CorrelationIdFilter filter;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private MockFilterChain chain;

    @BeforeEach
    void setUp() {
        filter = new CorrelationIdFilter();
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        chain = new MockFilterChain();
    }

    @AfterEach
    void tearDown() {
        MDC.clear();
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldGenerateCorrelationIdWhenNotPresent() throws Exception {
        filter.doFilterInternal(request, response, chain);

        assertThat(response.getHeader("X-Correlation-Id"))
            .isNotNull()
            .isNotBlank()
            .matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
    }

    @Test
    void shouldReuseCorrelationIdFromRequest() throws Exception {
        String existingId = "trace-abc-123";
        request.addHeader("X-Correlation-Id", existingId);

        filter.doFilterInternal(request, response, chain);

        assertThat(response.getHeader("X-Correlation-Id")).isEqualTo(existingId);
    }

    @Test
    void shouldClearMdcAfterRequest() throws Exception {
        filter.doFilterInternal(request, response, chain);

        assertThat(MDC.get("correlationId")).isNull();
        assertThat(MDC.get("userId")).isNull();
    }

    @Test
    void shouldSetCorrelationIdInMdcDuringRequest() throws Exception {
        var capturedId = new String[1];
        request.addHeader("X-Correlation-Id", "my-trace");
        var capturingChain = new MockFilterChain() {
            @Override
            public void doFilter(jakarta.servlet.ServletRequest req, jakarta.servlet.ServletResponse res)
                    throws java.io.IOException, jakarta.servlet.ServletException {
                capturedId[0] = MDC.get("correlationId");
                super.doFilter(req, res);
            }
        };

        filter.doFilterInternal(request, response, capturingChain);

        assertThat(capturedId[0]).isEqualTo("my-trace");
    }

    @Test
    void shouldGenerateNewIdWhenHeaderIsBlank() throws Exception {
        request.addHeader("X-Correlation-Id", "   ");

        filter.doFilterInternal(request, response, chain);

        assertThat(response.getHeader("X-Correlation-Id"))
            .isNotBlank()
            .matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
    }

    @Test
    void shouldSetUserIdInMdcWhenAuthenticated() throws Exception {
        var jwt = Jwt.withTokenValue("token")
            .header("alg", "RS256")
            .subject("user-42")
            .issuedAt(java.time.Instant.now())
            .expiresAt(java.time.Instant.now().plusSeconds(60))
            .build();
        var auth = new JwtAuthenticationToken(jwt, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);

        var capturedUserId = new String[1];
        var capturingChain = new MockFilterChain() {
            @Override
            public void doFilter(jakarta.servlet.ServletRequest req, jakarta.servlet.ServletResponse res)
                    throws java.io.IOException, jakarta.servlet.ServletException {
                capturedUserId[0] = MDC.get("userId");
            }
        };

        filter.doFilterInternal(request, response, capturingChain);

        assertThat(capturedUserId[0]).isEqualTo("user-42");
    }

    @Test
    void shouldSanitizeCRLFFromCorrelationIdHeader() throws Exception {
        request.addHeader("X-Correlation-Id", "valid-id\r\nX-Injected: header");

        filter.doFilterInternal(request, response, chain);

        assertThat(response.getHeader("X-Correlation-Id"))
            .doesNotContain("\r", "\n")
            .isEqualTo("valid-idX-Injected: header");
    }

    @Test
    void shouldGenerateNewIdWhenHeaderExceedsMaxLength() throws Exception {
        request.addHeader("X-Correlation-Id", "a".repeat(65));

        filter.doFilterInternal(request, response, chain);

        assertThat(response.getHeader("X-Correlation-Id"))
            .matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
    }
}
