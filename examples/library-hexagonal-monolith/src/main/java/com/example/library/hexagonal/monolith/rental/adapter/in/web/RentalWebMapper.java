package com.example.library.hexagonal.monolith.rental.adapter.in.web;

import com.example.library.hexagonal.monolith.rental.domain.Rental;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RentalWebMapper {

    RentalResponse toResponse(Rental rental);
}
