package com.example.library.modular.monolith.book.application.service;

import com.example.library.modular.monolith.book.api.BookInventory;
import com.example.library.modular.monolith.book.application.port.in.BookUseCase;
import com.example.library.modular.monolith.book.application.port.out.BookRepository;
import com.example.library.modular.monolith.book.domain.Book;
import com.example.library.modular.monolith.book.domain.BookNotFoundException;
import com.example.library.modular.monolith.book.domain.BookUnavailableException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BookApplicationService implements BookUseCase, BookInventory {

    private final BookRepository books;

    @Override
    @Transactional
    public Book create(Book book) {
        return books.save(book);
    }

    @Override
    @Transactional(readOnly = true)
    public Book findById(Long bookId) {
        return books.findById(bookId).orElseThrow(() -> new BookNotFoundException(bookId));
    }

    @Override
    @Transactional
    public void reserve(Long bookId) {
        if (books.reserve(bookId)) {
            return;
        }
        if (books.findById(bookId).isEmpty()) {
            throw new BookNotFoundException(bookId);
        }
        throw new BookUnavailableException(bookId);
    }

    @Override
    @Transactional
    public void release(Long bookId) {
        if (!books.release(bookId)) {
            throw new BookNotFoundException(bookId);
        }
    }
}
