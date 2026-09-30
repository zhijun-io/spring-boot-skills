package com.example.library.hexagonal.monolith.book.domain;

public record Book(
        Long id,
        String title,
        String author,
        String description,
        int totalCopies,
        int availableCopies) {
}
