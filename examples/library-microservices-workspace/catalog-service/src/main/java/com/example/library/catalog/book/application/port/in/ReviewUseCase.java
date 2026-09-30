package com.example.library.catalog.book.application.port.in;

import com.example.library.catalog.book.domain.Review;

public interface ReviewUseCase {

    Review create(Review review);
}
