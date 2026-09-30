package com.example.library.hexagonal.monolith.rental.domain;

public record Rental(Long id, Long bookId, String userId, RentalStatus status) {
}
