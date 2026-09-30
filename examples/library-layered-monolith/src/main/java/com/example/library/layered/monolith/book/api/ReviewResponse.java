package com.example.library.layered.monolith.book.api;

public record ReviewResponse(Long id, Long bookId, String userId, String comment, short rating) {
}
