package com.example.library.modular.monolith.book.adapter.in.web;

import com.example.library.modular.monolith.book.domain.Book;
import com.example.library.modular.monolith.book.domain.Review;
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
