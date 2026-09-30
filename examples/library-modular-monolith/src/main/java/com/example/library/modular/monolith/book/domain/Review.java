package com.example.library.modular.monolith.book.domain;

public record Review(
        Long id,
        Long bookId,
        String userId,
        String comment,
        short rating) {
}
