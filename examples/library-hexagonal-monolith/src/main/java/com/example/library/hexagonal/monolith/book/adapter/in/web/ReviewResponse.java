package com.example.library.hexagonal.monolith.book.adapter.in.web;

public record ReviewResponse(Long id, Long bookId, String userId, String comment, short rating) {
}
