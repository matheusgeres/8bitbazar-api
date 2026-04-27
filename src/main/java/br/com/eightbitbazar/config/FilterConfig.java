package br.com.eightbitbazar.config;

import br.com.eightbitbazar.adapter.in.web.filter.CorrelationIdFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean<CorrelationIdFilter> correlationIdFilterRegistration(
            CorrelationIdFilter filter) {
        var reg = new FilterRegistrationBean<>(filter);
        reg.setOrder(-50);
        return reg;
    }
}
