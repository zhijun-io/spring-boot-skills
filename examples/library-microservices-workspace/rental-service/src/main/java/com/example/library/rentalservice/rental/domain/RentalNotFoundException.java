package com.example.library.rentalservice.rental.domain;

import com.example.library.rentalservice.shared.error.ApplicationException;

public class RentalNotFoundException extends ApplicationException {

    public RentalNotFoundException(Long rentalId) {
        super(404, "RENTAL_NOT_FOUND", "rental.not-found");
    }
}
