package com.example.library.layered.monolith.rental.web;

import com.example.library.layered.monolith.rental.api.RentalCreateRequest;
import com.example.library.layered.monolith.rental.api.RentalResponse;
import com.example.library.layered.monolith.rental.service.RentalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rentals")
@RequiredArgsConstructor
public class RentalController {

    private final RentalService rentalService;
    private final RentalWebMapper mapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RentalResponse rent(@Valid @RequestBody RentalCreateRequest request) {
        return mapper.toResponse(rentalService.rent(mapper.toDomain(request)));
    }

    @PostMapping("/{rentalId}/return")
    public RentalResponse returnRental(@PathVariable Long rentalId) {
        return mapper.toResponse(rentalService.returnRental(rentalId));
    }
}
