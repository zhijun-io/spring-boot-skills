package com.example.library.hexagonal.monolith.rental.adapter.in.web;

import com.example.library.hexagonal.monolith.rental.domain.RentalStatus;

public record RentalResponse(Long id, Long bookId, String userId, RentalStatus status) {
}
