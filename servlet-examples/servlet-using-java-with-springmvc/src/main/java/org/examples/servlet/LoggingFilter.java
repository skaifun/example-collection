package org.examples.servlet;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.logging.Logger;

public class LoggingFilter extends OncePerRequestFilter {
    public static final Logger LOGGER = Logger.getLogger(LoggingFilter.class.getSimpleName());

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        long start = System.currentTimeMillis();
        filterChain.doFilter(request, response);
        long duration = System.currentTimeMillis() - start;

        LOGGER.info("============================");
        LOGGER.info("Request URL: " + request.getRequestURL());
        LOGGER.info("Method: " + request.getMethod());
        LOGGER.info("Query String: " + request.getQueryString());
        LOGGER.info("Processing time: " + duration + " ms");
    }
}
