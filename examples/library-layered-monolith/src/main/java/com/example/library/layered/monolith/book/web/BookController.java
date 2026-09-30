package com.example.library.layered.monolith.book.web;

import com.example.library.layered.monolith.book.api.BookCreateRequest;
import com.example.library.layered.monolith.book.api.BookResponse;
import com.example.library.layered.monolith.book.api.ReviewCreateRequest;
import com.example.library.layered.monolith.book.api.ReviewResponse;
import com.example.library.layered.monolith.book.service.BookService;
import com.example.library.layered.monolith.book.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;
    private final ReviewService reviewService;
    private final BookWebMapper mapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookResponse create(@Valid @RequestBody BookCreateRequest request) {
        return mapper.toResponse(bookService.create(mapper.toDomain(request)));
    }

    @GetMapping("/{bookId}")
    public BookResponse findById(@PathVariable Long bookId) {
        return mapper.toResponse(bookService.findById(bookId));
    }

    @PostMapping("/{bookId}/reviews")
    @ResponseStatus(HttpStatus.CREATED)
    public ReviewResponse review(
            @PathVariable Long bookId,
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody ReviewCreateRequest request) {
        var review = mapper.toDomain(request);
        review = new com.example.library.layered.monolith.book.domain.Review(
                null, bookId, userId, review.comment(), review.rating());
        return mapper.toResponse(reviewService.create(review));
    }
}
