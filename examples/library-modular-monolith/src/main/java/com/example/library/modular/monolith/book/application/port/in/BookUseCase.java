package com.example.library.modular.monolith.book.application.port.in;

import com.example.library.modular.monolith.book.domain.Book;

public interface BookUseCase {

    Book create(Book book);

    Book findById(Long bookId);
}
