package com.example.library.catalog.book.domain;

import com.example.library.catalog.shared.error.ApplicationException;

public class BookUnavailableException extends ApplicationException {

    public BookUnavailableException(Long bookId) {
        super(409, "BOOK_UNAVAILABLE", "book.unavailable");
    }
}
