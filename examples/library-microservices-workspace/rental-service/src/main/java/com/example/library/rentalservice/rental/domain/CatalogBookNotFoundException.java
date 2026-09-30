package com.example.library.rentalservice.rental.domain;

import com.example.library.rentalservice.shared.error.ApplicationException;

public class CatalogBookNotFoundException extends ApplicationException {

    public CatalogBookNotFoundException(Long bookId) {
        super(404, "BOOK_NOT_FOUND", "book.not-found");
    }
}
