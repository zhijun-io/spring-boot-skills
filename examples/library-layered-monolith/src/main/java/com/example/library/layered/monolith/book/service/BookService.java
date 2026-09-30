package com.example.library.layered.monolith.book.service;

import com.example.library.layered.monolith.book.domain.Book;
import com.example.library.layered.monolith.book.domain.BookNotFoundException;
import com.example.library.layered.monolith.book.domain.BookUnavailableException;
import com.example.library.layered.monolith.book.persistence.BookPersistenceMapper;
import com.example.library.layered.monolith.book.persistence.BookMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BookService implements BookInventory {

    private final BookMapper bookMapper;
    private final BookPersistenceMapper entityMapper;

    @Transactional
    public Book create(Book book) {
        var entity = entityMapper.toEntity(book);
        bookMapper.insert(entity);
        return entityMapper.toDomain(entity);
    }

    @Transactional(readOnly = true)
    public Book findById(Long bookId) {
        var entity = bookMapper.selectById(bookId);
        if (entity == null) {
            throw new BookNotFoundException(bookId);
        }
        return entityMapper.toDomain(entity);
    }

    @Override
    @Transactional
    public void reserve(Long bookId) {
        if (bookMapper.reserve(bookId) == 1) {
            return;
        }
        if (bookMapper.selectById(bookId) == null) {
            throw new BookNotFoundException(bookId);
        }
        throw new BookUnavailableException(bookId);
    }

    @Override
    @Transactional
    public void release(Long bookId) {
        if (bookMapper.release(bookId) == 1) {
            return;
        }
        if (bookMapper.selectById(bookId) == null) {
            throw new BookNotFoundException(bookId);
        }
        throw new IllegalStateException("Book inventory cannot be released");
    }
}
