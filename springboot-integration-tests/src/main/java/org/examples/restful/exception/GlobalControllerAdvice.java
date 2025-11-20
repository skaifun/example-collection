package org.examples.restful.exception;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Order(0)
@RestControllerAdvice
public class GlobalControllerAdvice {

    @ExceptionHandler(AppException.class)
    public ProblemDetail handleAppException(RuntimeException e) {
        log.error("something happened: {}", e.getMessage(), e);
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.info("method argument not valid: {}", e.getMessage());

        Map<String, List<String>> fieldErrors = new HashMap<>();
        e.getBindingResult().getFieldErrors().forEach(fieldError ->
            fieldErrors
                .computeIfAbsent(fieldError.getField(), k -> new ArrayList<>())
                .add(fieldError.getDefaultMessage())
        );

        ProblemDetail problemDetail = e.getBody();
        problemDetail.setProperty("errors", fieldErrors);
        return problemDetail;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleException(Exception e) {
        log.error("something unknown happened: {}", e.getMessage(), e);
        return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
    }
}
