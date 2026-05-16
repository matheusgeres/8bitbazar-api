package br.com.eightbitbazar.adapter.in.web.filter;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

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

    @Test
    void shouldRestorePreviousMdcAfterRequest() throws Exception {
        MDC.put("existing", "value");

        filter.doFilterInternal(request, response, chain);

        assertThat(MDC.get("existing")).isEqualTo("value");
        assertThat(MDC.get("correlationId")).isNull();
    }
}
