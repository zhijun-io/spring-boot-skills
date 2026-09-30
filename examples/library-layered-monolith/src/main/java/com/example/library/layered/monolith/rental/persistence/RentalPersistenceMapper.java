package com.example.library.layered.monolith.rental.persistence;

import com.example.library.layered.monolith.rental.domain.Rental;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RentalPersistenceMapper {

    Rental toDomain(RentalEntity entity);

    RentalEntity toEntity(Rental rental);
}
