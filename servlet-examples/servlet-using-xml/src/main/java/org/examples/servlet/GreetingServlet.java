package org.examples.servlet;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

public class GreetingServlet extends HttpServlet {
    public static final Logger LOGGER = Logger.getLogger(GreetingServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String name = req.getParameter("name");
        LOGGER.info("request with params: name=%s".formatted(name));

        if (name == null || name.isEmpty()) {
            name = "world";
        }
        String message = "hello, " + name;

        resp.setContentType("text/plain");
        resp.setCharacterEncoding(StandardCharsets.UTF_8);
        resp.setStatus(HttpServletResponse.SC_OK);

        try (PrintWriter writer = resp.getWriter()) {
            writer.print(message);
        }
    }
}
