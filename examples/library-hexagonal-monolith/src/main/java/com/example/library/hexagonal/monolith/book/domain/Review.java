package com.example.library.hexagonal.monolith.book.domain;

public record Review(
        Long id,
        Long bookId,
        String userId,
        String comment,
        short rating) {
}
