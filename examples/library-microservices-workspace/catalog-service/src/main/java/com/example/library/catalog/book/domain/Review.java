package com.example.library.catalog.book.domain;

public record Review(
        Long id,
        Long bookId,
        String userId,
        String comment,
        short rating) {
}
