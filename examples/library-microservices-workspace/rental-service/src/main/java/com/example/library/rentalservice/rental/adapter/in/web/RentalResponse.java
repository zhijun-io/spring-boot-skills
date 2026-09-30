package com.example.library.rentalservice.rental.adapter.in.web;

import com.example.library.rentalservice.rental.domain.RentalStatus;

public record RentalResponse(Long id, Long bookId, String userId, RentalStatus status) {
}
