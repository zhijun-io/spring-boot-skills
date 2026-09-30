package com.example.library.modular.monolith.rental.adapter.in.web;

import com.example.library.modular.monolith.rental.application.port.in.RentalUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rentals")
@RequiredArgsConstructor
public class RentalController {

    private final RentalUseCase rentals;
    private final RentalWebMapper mapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RentalResponse rent(@Valid @RequestBody RentalCreateRequest request) {
        return mapper.toResponse(rentals.rent(request.bookId(), request.userId()));
    }

    @PostMapping("/{rentalId}/return")
    public RentalResponse returnRental(@PathVariable Long rentalId) {
        return mapper.toResponse(rentals.returnRental(rentalId));
    }
}
