package com.example.library.layered.monolith.rental.api;

import com.example.library.layered.monolith.rental.domain.RentalStatus;

public record RentalResponse(Long id, Long bookId, String userId, RentalStatus status) {
}
