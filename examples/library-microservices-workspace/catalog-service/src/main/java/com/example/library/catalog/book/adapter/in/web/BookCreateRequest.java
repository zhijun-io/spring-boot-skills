package com.example.library.catalog.book.adapter.in.web;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record BookCreateRequest(
        @NotBlank(message = "{book.title.required}") String title,
        @NotBlank(message = "{book.author.required}") String author,
        String description,
        @NotNull(message = "{book.copies.required}")
        @Min(value = 1, message = "{book.copies.positive}") Integer totalCopies) {
}
