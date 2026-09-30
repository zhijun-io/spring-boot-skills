package com.example.library.modular.monolith.rental.adapter.in.web;

import com.example.library.modular.monolith.rental.domain.Rental;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RentalWebMapper {

    RentalResponse toResponse(Rental rental);
}
