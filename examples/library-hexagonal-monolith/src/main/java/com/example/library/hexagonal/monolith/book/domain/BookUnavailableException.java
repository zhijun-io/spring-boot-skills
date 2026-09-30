package com.example.library.hexagonal.monolith.book.domain;

import com.example.library.hexagonal.monolith.shared.error.ApplicationException;

public class BookUnavailableException extends ApplicationException {

    public BookUnavailableException(Long bookId) {
        super(409, "BOOK_UNAVAILABLE", "book.unavailable");
    }
}
