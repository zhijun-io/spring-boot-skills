package com.example.library.layered.monolith.rental.domain;

public record Rental(Long id, Long bookId, String userId, RentalStatus status) {
}
