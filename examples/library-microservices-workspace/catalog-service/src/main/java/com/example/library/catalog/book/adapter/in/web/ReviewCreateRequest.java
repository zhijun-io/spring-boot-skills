package com.example.library.catalog.book.adapter.in.web;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record ReviewCreateRequest(
        @NotBlank(message = "{review.comment.required}") String comment,
        @Min(value = 1, message = "{review.rating.range}")
        @Max(value = 5, message = "{review.rating.range}") short rating) {
}
