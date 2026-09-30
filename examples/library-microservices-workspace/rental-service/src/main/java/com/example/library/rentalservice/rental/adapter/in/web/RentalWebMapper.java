package com.example.library.rentalservice.rental.adapter.in.web;

import com.example.library.rentalservice.rental.domain.Rental;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RentalWebMapper {

    RentalResponse toResponse(Rental rental);
}
