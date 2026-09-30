package com.example.library.layered.monolith.rental.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RentalCreateRequest(
        @NotNull(message = "{rental.book.required}") Long bookId,
        @NotBlank(message = "{rental.user.required}") String userId) {
}
