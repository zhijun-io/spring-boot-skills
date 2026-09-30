package com.example.library.catalog.book.api;

public interface BookInventory {

    void reserve(Long bookId);

    void release(Long bookId);
}
