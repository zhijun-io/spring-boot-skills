package com.example.library.layered.monolith.book.domain;

public record Book(
        Long id,
        String title,
        String author,
        String description,
        int totalCopies,
        int availableCopies) {
}
