package com.example.library.layered.monolith.book.domain;

import com.example.library.layered.monolith.shared.error.ApplicationException;

public class BookUnavailableException extends ApplicationException {

    public BookUnavailableException(Long bookId) {
        super(409, "BOOK_UNAVAILABLE", "book.unavailable");
    }
}
