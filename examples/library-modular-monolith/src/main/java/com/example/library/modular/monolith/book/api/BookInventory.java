package com.example.library.modular.monolith.book.api;

public interface BookInventory {

    void reserve(Long bookId);

    void release(Long bookId);
}
