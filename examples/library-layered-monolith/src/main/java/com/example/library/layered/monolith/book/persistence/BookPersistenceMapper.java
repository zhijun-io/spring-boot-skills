package com.example.library.layered.monolith.book.persistence;

import com.example.library.layered.monolith.book.domain.Book;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BookPersistenceMapper {

    Book toDomain(BookEntity entity);

    @Mapping(target = "id", ignore = true)
    BookEntity toEntity(Book book);
}
