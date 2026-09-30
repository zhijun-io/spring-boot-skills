package com.example.library.catalog.book.adapter.in.web;

import com.example.library.catalog.book.api.BookInventory;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/books")
@RequiredArgsConstructor
public class BookInventoryController {

    private final BookInventory inventory;

    @PostMapping("/{bookId}/reserve")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reserve(@PathVariable Long bookId) {
        inventory.reserve(bookId);
    }

    @PostMapping("/{bookId}/release")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void release(@PathVariable Long bookId) {
        inventory.release(bookId);
    }
}
