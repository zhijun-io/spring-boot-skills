package com.example.library.catalog.book.application.port.in;

import com.example.library.catalog.book.domain.Book;

public interface BookUseCase {

    Book create(Book book);

    Book findById(Long bookId);
}
