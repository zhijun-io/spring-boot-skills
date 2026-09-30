package com.example.library.modular.monolith.rental.adapter.in.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RentalCreateRequest(
        @NotNull(message = "{rental.book.required}") Long bookId,
        @NotBlank(message = "{rental.user.required}") String userId) {
}
