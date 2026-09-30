package com.example.library.hexagonal.monolith.shared.web;

import com.example.library.hexagonal.monolith.shared.error.ApplicationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import java.util.Locale;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    private final MessageSource messages;

    public ApiExceptionHandler(MessageSource messages) {
        this.messages = messages;
    }

    @ExceptionHandler(ApplicationException.class)
    ProblemDetail applicationFailure(ApplicationException exception, Locale locale) {
        var detail = ProblemDetail.forStatusAndDetail(
                HttpStatusCode.valueOf(exception.status()),
                messages.getMessage(exception.messageKey(), null, locale));
        detail.setTitle("Application request failed");
        detail.setProperty("code", exception.code());
        return detail;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail validation(MethodArgumentNotValidException exception, Locale locale) {
        var message = exception.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> messages.getMessage(error, locale))
                .orElseGet(() -> messages.getMessage("request.invalid", null, locale));
        return problem(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", message, "Invalid request");
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ProblemDetail malformed(HttpMessageNotReadableException exception, Locale locale) {
        return problem(HttpStatus.BAD_REQUEST, "MALFORMED_REQUEST",
                messages.getMessage("request.malformed", null, locale), "Malformed request");
    }

    @ExceptionHandler(Exception.class)
    ProblemDetail unexpected(Exception exception, Locale locale) {
        log.error("Unexpected API failure", exception);
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR",
                messages.getMessage("error.internal", null, locale), "Internal server error");
    }

    private ProblemDetail problem(HttpStatusCode status, String code, String detail, String title) {
        var problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        problem.setProperty("code", code);
        return problem;
    }
}
