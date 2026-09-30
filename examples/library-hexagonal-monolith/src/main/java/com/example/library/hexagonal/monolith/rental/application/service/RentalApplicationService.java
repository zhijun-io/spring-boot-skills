package com.example.library.hexagonal.monolith.rental.application.service;

import com.example.library.hexagonal.monolith.book.application.port.in.BookInventory;
import com.example.library.hexagonal.monolith.rental.application.port.in.RentalUseCase;
import com.example.library.hexagonal.monolith.rental.application.port.out.RentalRepository;
import com.example.library.hexagonal.monolith.rental.domain.Rental;
import com.example.library.hexagonal.monolith.rental.domain.RentalNotFoundException;
import com.example.library.hexagonal.monolith.rental.domain.RentalStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RentalApplicationService implements RentalUseCase {

    private final BookInventory inventory;
    private final RentalRepository rentals;

    @Override
    @Transactional
    public Rental rent(Long bookId, String userId) {
        inventory.reserve(bookId);
        return rentals.save(new Rental(null, bookId, userId, RentalStatus.RENTED));
    }

    @Override
    @Transactional
    public Rental returnRental(Long rentalId) {
        var rental = rentals.findById(rentalId)
                .orElseThrow(() -> new RentalNotFoundException(rentalId));
        if (rental.status() == RentalStatus.RETURNED) {
            return rental;
        }
        if (!rentals.markReturned(rentalId)) {
            var current = rentals.findById(rentalId)
                    .orElseThrow(() -> new RentalNotFoundException(rentalId));
            if (current.status() == RentalStatus.RETURNED) {
                return current;
            }
            throw new IllegalStateException("Rental could not be returned");
        }
        inventory.release(rental.bookId());
        return rentals.findById(rentalId)
                .orElseThrow(() -> new RentalNotFoundException(rentalId));
    }
}
