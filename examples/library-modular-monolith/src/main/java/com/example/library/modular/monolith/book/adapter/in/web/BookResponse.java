package com.example.library.modular.monolith.book.adapter.in.web;

public record BookResponse(
        Long id,
        String title,
        String author,
        String description,
        int totalCopies,
        int availableCopies) {
}
