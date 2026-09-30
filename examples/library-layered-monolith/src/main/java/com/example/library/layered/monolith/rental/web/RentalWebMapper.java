package com.example.library.layered.monolith.rental.web;

import com.example.library.layered.monolith.rental.api.RentalCreateRequest;
import com.example.library.layered.monolith.rental.api.RentalResponse;
import com.example.library.layered.monolith.rental.domain.Rental;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RentalWebMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", constant = "RENTED")
    Rental toDomain(RentalCreateRequest request);

    RentalResponse toResponse(Rental rental);
}
