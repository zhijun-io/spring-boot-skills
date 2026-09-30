package com.example.library.layered.monolith.rental.domain;

import com.example.library.layered.monolith.shared.error.ApplicationException;

public class RentalNotFoundException extends ApplicationException {

    public RentalNotFoundException(Long rentalId) {
        super(404, "RENTAL_NOT_FOUND", "rental.not-found");
    }
}
