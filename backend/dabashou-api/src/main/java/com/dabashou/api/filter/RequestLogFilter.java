package com.dabashou.api.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * 请求日志过滤器 — 记录method/uri/status/duration，MDC注入traceId
 */
@Component
public class RequestLogFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestLogFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String traceId = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        MDC.put("traceId", traceId);

        long start = System.currentTimeMillis();
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String query = request.getQueryString();

        try {
            filterChain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - start;
            int status = response.getStatus();
            String fullUri = query != null ? uri + "?" + query : uri;
            if (duration > 3000) {
                log.warn("慢请求 {} {} → {} ({}ms)", method, fullUri, status, duration);
            } else {
                log.info("{} {} → {} ({}ms)", method, fullUri, status, duration);
            }
            MDC.remove("traceId");
        }
    }
}
