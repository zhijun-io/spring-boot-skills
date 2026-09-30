package com.example.library.layered.monolith.book.service;

public interface BookInventory {

    void reserve(Long bookId);

    void release(Long bookId);
}
