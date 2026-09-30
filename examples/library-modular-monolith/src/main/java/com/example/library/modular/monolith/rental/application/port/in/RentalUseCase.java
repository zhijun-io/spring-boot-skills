package com.example.library.modular.monolith.rental.application.port.in;

import com.example.library.modular.monolith.rental.domain.Rental;

public interface RentalUseCase {

    Rental rent(Long bookId, String userId);

    Rental returnRental(Long rentalId);
}
