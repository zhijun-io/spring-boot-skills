package com.example.library.hexagonal.monolith.book.application.port.in;

public interface BookInventory {

    void reserve(Long bookId);

    void release(Long bookId);
}
