package com.example.library.hexagonal.monolith.book.application.port.in;

import com.example.library.hexagonal.monolith.book.domain.Review;

public interface ReviewUseCase {

    Review create(Review review);
}
