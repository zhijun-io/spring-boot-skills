package com.example.library.layered.monolith.book.web;

import com.example.library.layered.monolith.book.api.BookCreateRequest;
import com.example.library.layered.monolith.book.api.BookResponse;
import com.example.library.layered.monolith.book.api.ReviewCreateRequest;
import com.example.library.layered.monolith.book.api.ReviewResponse;
import com.example.library.layered.monolith.book.domain.Book;
import com.example.library.layered.monolith.book.domain.Review;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BookWebMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "availableCopies", source = "totalCopies")
    Book toDomain(BookCreateRequest request);

    BookResponse toResponse(Book book);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "bookId", ignore = true)
    @Mapping(target = "userId", ignore = true)
    Review toDomain(ReviewCreateRequest request);

    ReviewResponse toResponse(Review review);
}
