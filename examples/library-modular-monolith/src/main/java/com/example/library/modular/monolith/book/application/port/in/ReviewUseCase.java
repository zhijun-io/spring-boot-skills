package com.example.library.modular.monolith.book.application.port.in;

import com.example.library.modular.monolith.book.domain.Review;

public interface ReviewUseCase {

    Review create(Review review);
}
