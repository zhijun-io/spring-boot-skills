package com.example.library.modular.monolith.rental.domain;

public record Rental(Long id, Long bookId, String userId, RentalStatus status) {
}
