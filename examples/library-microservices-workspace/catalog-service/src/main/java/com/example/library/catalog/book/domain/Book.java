package com.example.library.catalog.book.domain;

public record Book(
        Long id,
        String title,
        String author,
        String description,
        int totalCopies,
        int availableCopies) {
}
