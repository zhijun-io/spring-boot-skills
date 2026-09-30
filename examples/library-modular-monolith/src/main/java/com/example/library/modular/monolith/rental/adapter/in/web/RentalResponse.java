package com.example.library.modular.monolith.rental.adapter.in.web;

import com.example.library.modular.monolith.rental.domain.RentalStatus;

public record RentalResponse(Long id, Long bookId, String userId, RentalStatus status) {
}
