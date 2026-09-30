package com.example.library.hexagonal.monolith.book.application.port.in;

import com.example.library.hexagonal.monolith.book.domain.Book;

public interface BookUseCase {

    Book create(Book book);

    Book findById(Long bookId);
}
