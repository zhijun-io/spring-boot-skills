package com.example.library.catalog.book.adapter.in.web;

import com.example.library.catalog.book.application.port.in.BookUseCase;
import com.example.library.catalog.book.application.port.in.ReviewUseCase;
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

    private final BookUseCase books;
    private final ReviewUseCase reviews;
    private final BookWebMapper mapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookResponse create(@Valid @RequestBody BookCreateRequest request) {
        return mapper.toResponse(books.create(mapper.toDomain(request)));
    }

    @GetMapping("/{bookId}")
    public BookResponse findById(@PathVariable Long bookId) {
        return mapper.toResponse(books.findById(bookId));
    }

    @PostMapping("/{bookId}/reviews")
    @ResponseStatus(HttpStatus.CREATED)
    public ReviewResponse review(
            @PathVariable Long bookId,
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody ReviewCreateRequest request) {
        var review = mapper.toDomain(request);
        var withOwner = new com.example.library.catalog.book.domain.Review(
                null, bookId, userId, review.comment(), review.rating());
        return mapper.toResponse(reviews.create(withOwner));
    }
}
