package org.examples.servlet;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.util.logging.Logger;

public class LoggingFilter implements Filter {
    public static final Logger LOGGER = Logger.getLogger(LoggingFilter.class.getName());

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        long start = System.currentTimeMillis();
        filterChain.doFilter(servletRequest, servletResponse);
        long duration = System.currentTimeMillis() - start;

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        LOGGER.info("============================");
        LOGGER.info("Request URL: " + request.getRequestURL());
        LOGGER.info("Method: " + request.getMethod());
        LOGGER.info("Query String: " + request.getQueryString());
        LOGGER.info("Processing time: " + duration + " ms");
    }

}
