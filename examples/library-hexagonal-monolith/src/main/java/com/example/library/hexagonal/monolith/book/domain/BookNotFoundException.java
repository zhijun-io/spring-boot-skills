package com.example.library.hexagonal.monolith.book.domain;

import com.example.library.hexagonal.monolith.shared.error.ApplicationException;

public class BookNotFoundException extends ApplicationException {

    public BookNotFoundException(Long bookId) {
        super(404, "BOOK_NOT_FOUND", "book.not-found");
    }
}
