package com.example.library.rentalservice.rental.domain;

import com.example.library.rentalservice.shared.error.ApplicationException;

public class BookUnavailableException extends ApplicationException {

    public BookUnavailableException(Long bookId) {
        super(409, "BOOK_UNAVAILABLE", "book.unavailable");
    }
}
