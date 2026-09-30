package com.example.library.layered.monolith.shared.web;

import com.example.library.layered.monolith.shared.error.ApplicationException;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Locale;

@RestControllerAdvice
public class ApiExceptionHandler {

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
                .orElse("Request validation failed");
        var detail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, message);
        detail.setTitle("Invalid request");
        detail.setProperty("code", "VALIDATION_FAILED");
        return detail;
    }
}
