package com.example.library.layered.monolith.book.api;

public record BookResponse(
        Long id,
        String title,
        String author,
        String description,
        int totalCopies,
        int availableCopies) {
}
