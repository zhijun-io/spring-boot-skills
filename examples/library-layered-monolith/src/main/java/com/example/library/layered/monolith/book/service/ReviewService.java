package com.example.library.layered.monolith.book.service;

import com.example.library.layered.monolith.book.domain.Review;
import com.example.library.layered.monolith.book.persistence.ReviewPersistenceMapper;
import com.example.library.layered.monolith.book.persistence.ReviewMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewMapper reviewMapper;
    private final ReviewPersistenceMapper entityMapper;
    private final BookService books;

    @Transactional
    public Review create(Review review) {
        books.findById(review.bookId());
        var entity = entityMapper.toEntity(review);
        reviewMapper.insert(entity);
        return entityMapper.toDomain(entity);
    }
}
