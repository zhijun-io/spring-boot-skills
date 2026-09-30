package com.example.library.rentalservice.rental.application.port.in;

import com.example.library.rentalservice.rental.domain.Rental;

public interface RentalUseCase {

    Rental rent(Long bookId, String userId);

    Rental returnRental(Long rentalId);
}
