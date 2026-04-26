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

    @BeforeEach
    void setUp() {
        filter = new CorrelationIdFilter();
    }

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    void shouldGenerateCorrelationIdWhenNotPresent() throws Exception {
        var request = new MockHttpServletRequest();
        var response = new MockHttpServletResponse();
        var chain = new MockFilterChain();

        filter.doFilterInternal(request, response, chain);

        assertThat(response.getHeader("X-Correlation-Id"))
            .isNotNull()
            .isNotBlank()
            .matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
    }

    @Test
    void shouldReuseCorrelationIdFromRequest() throws Exception {
        String existingId = "trace-abc-123";
        var request = new MockHttpServletRequest();
        request.addHeader("X-Correlation-Id", existingId);
        var response = new MockHttpServletResponse();
        var chain = new MockFilterChain();

        filter.doFilterInternal(request, response, chain);

        assertThat(response.getHeader("X-Correlation-Id")).isEqualTo(existingId);
    }

    @Test
    void shouldClearMdcAfterRequest() throws Exception {
        var request = new MockHttpServletRequest();
        var response = new MockHttpServletResponse();
        var chain = new MockFilterChain();

        filter.doFilterInternal(request, response, chain);

        assertThat(MDC.get("correlationId")).isNull();
        assertThat(MDC.get("userId")).isNull();
    }

    @Test
    void shouldSetCorrelationIdInMdcDuringRequest() throws Exception {
        var capturedId = new String[1];
        var request = new MockHttpServletRequest();
        request.addHeader("X-Correlation-Id", "my-trace");
        var response = new MockHttpServletResponse();
        var chain = new MockFilterChain() {
            @Override
            public void doFilter(jakarta.servlet.ServletRequest req, jakarta.servlet.ServletResponse res)
                    throws java.io.IOException, jakarta.servlet.ServletException {
                capturedId[0] = MDC.get("correlationId");
                super.doFilter(req, res);
            }
        };

        filter.doFilterInternal(request, response, chain);

        assertThat(capturedId[0]).isEqualTo("my-trace");
    }
}
