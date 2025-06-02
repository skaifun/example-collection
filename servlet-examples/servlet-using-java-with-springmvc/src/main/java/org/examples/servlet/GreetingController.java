package org.examples.servlet;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.logging.Logger;

@Controller
public class GreetingController {

    private static final Logger LOGGER = Logger.getLogger(GreetingController.class.getSimpleName());

    @GetMapping(value = "/hello", produces = MediaType.TEXT_PLAIN_VALUE)
    @ResponseBody
    public String sayHello(@RequestParam(value = "name", defaultValue = "world", required = false) String name) {
        LOGGER.info("request with params: name=%s".formatted(name));
        return "hello, " + name;
    }
}