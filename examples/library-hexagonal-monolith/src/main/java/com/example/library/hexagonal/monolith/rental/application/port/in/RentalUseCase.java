package com.example.library.hexagonal.monolith.rental.application.port.in;

import com.example.library.hexagonal.monolith.rental.domain.Rental;

public interface RentalUseCase {

    Rental rent(Long bookId, String userId);

    Rental returnRental(Long rentalId);
}
