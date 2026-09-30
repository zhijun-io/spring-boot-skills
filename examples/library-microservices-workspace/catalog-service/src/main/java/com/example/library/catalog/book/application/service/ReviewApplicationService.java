package com.example.library.catalog.book.application.service;

import com.example.library.catalog.book.application.port.in.BookUseCase;
import com.example.library.catalog.book.application.port.in.ReviewUseCase;
import com.example.library.catalog.book.application.port.out.ReviewRepository;
import com.example.library.catalog.book.domain.Review;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewApplicationService implements ReviewUseCase {

    private final BookUseCase books;
    private final ReviewRepository reviews;

    @Override
    @Transactional
    public Review create(Review review) {
        books.findById(review.bookId());
        return reviews.save(review);
    }
}
