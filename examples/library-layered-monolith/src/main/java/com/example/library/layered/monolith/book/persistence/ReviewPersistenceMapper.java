package com.example.library.layered.monolith.book.persistence;

import com.example.library.layered.monolith.book.domain.Review;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ReviewPersistenceMapper {

    Review toDomain(ReviewEntity entity);

    ReviewEntity toEntity(Review review);
}
