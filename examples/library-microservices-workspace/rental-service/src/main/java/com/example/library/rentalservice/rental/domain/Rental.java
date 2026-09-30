package com.example.library.rentalservice.rental.domain;

public record Rental(Long id, Long bookId, String userId, RentalStatus status) {
}
