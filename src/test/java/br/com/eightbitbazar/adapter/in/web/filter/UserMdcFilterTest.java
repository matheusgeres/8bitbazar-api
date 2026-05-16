package br.com.eightbitbazar.adapter.in.web.filter;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserMdcFilterTest {

    private UserMdcFilter filter;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        filter = new UserMdcFilter();
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @AfterEach
    void tearDown() {
        MDC.clear();
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldSetUserIdInMdcWhenPrincipalIsJwt() throws Exception {
        var jwt = Jwt.withTokenValue("token")
            .header("alg", "RS256")
            .subject("user-42")
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(60))
            .build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt, List.of()));

        var capturedUserId = new String[1];
        var chain = new MockFilterChain() {
            @Override
            public void doFilter(jakarta.servlet.ServletRequest req, jakarta.servlet.ServletResponse res)
                    throws java.io.IOException, jakarta.servlet.ServletException {
                capturedUserId[0] = MDC.get("userId");
                super.doFilter(req, res);
            }
        };

        filter.doFilterInternal(request, response, chain);

        assertThat(capturedUserId[0]).isEqualTo("user-42");
    }

    @Test
    void shouldNotSetUserIdWhenAuthenticationIsAbsent() throws Exception {
        var capturedUserId = new String[1];
        var chain = new MockFilterChain() {
            @Override
            public void doFilter(jakarta.servlet.ServletRequest req, jakarta.servlet.ServletResponse res)
                    throws java.io.IOException, jakarta.servlet.ServletException {
                capturedUserId[0] = MDC.get("userId");
                super.doFilter(req, res);
            }
        };

        filter.doFilterInternal(request, response, chain);

        assertThat(capturedUserId[0]).isNull();
    }

    @Test
    void shouldNotSetUserIdWhenPrincipalIsNotJwt() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
            new TestingAuthenticationToken("principal", "credentials")
        );

        var capturedUserId = new String[1];
        var chain = new MockFilterChain() {
            @Override
            public void doFilter(jakarta.servlet.ServletRequest req, jakarta.servlet.ServletResponse res)
                    throws java.io.IOException, jakarta.servlet.ServletException {
                capturedUserId[0] = MDC.get("userId");
                super.doFilter(req, res);
            }
        };

        filter.doFilterInternal(request, response, chain);

        assertThat(capturedUserId[0]).isNull();
    }

    @Test
    void shouldPreserveExistingCorrelationIdDuringRequest() throws Exception {
        MDC.put("correlationId", "trace-123");
        var jwt = Jwt.withTokenValue("token")
            .header("alg", "RS256")
            .subject("user-42")
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(60))
            .build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt, List.of()));

        var capturedCorrelationId = new String[1];
        var capturedUserId = new String[1];
        var chain = new MockFilterChain() {
            @Override
            public void doFilter(jakarta.servlet.ServletRequest req, jakarta.servlet.ServletResponse res)
                    throws java.io.IOException, jakarta.servlet.ServletException {
                capturedCorrelationId[0] = MDC.get("correlationId");
                capturedUserId[0] = MDC.get("userId");
                super.doFilter(req, res);
            }
        };

        filter.doFilterInternal(request, response, chain);

        assertThat(capturedCorrelationId[0]).isEqualTo("trace-123");
        assertThat(capturedUserId[0]).isEqualTo("user-42");
    }

    @Test
    void shouldRestorePreviousMdcAfterRequest() throws Exception {
        MDC.put("correlationId", "trace-123");
        MDC.put("existing", "value");
        var jwt = Jwt.withTokenValue("token")
            .header("alg", "RS256")
            .subject("user-42")
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(60))
            .build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt, List.of()));

        filter.doFilterInternal(request, response, new MockFilterChain());

        assertThat(MDC.get("correlationId")).isEqualTo("trace-123");
        assertThat(MDC.get("existing")).isEqualTo("value");
        assertThat(MDC.get("userId")).isNull();
    }

    @Test
    void shouldRestorePreviousMdcWhenChainThrows() {
        MDC.put("correlationId", "trace-123");
        var jwt = Jwt.withTokenValue("token")
            .header("alg", "RS256")
            .subject("user-42")
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(60))
            .build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt, List.of()));
        var failingChain = new MockFilterChain() {
            @Override
            public void doFilter(jakarta.servlet.ServletRequest req, jakarta.servlet.ServletResponse res)
                    throws java.io.IOException {
                throw new java.io.IOException("boom");
            }
        };

        assertThatThrownBy(() -> filter.doFilterInternal(request, response, failingChain))
            .isInstanceOf(java.io.IOException.class)
            .hasMessage("boom");

        assertThat(MDC.get("correlationId")).isEqualTo("trace-123");
        assertThat(MDC.get("userId")).isNull();
    }
}
