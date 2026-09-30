package com.example.library.modular.monolith.rental.application.service;

import com.example.library.modular.monolith.book.api.BookInventory;
import com.example.library.modular.monolith.rental.application.port.in.RentalUseCase;
import com.example.library.modular.monolith.rental.application.port.out.RentalRepository;
import com.example.library.modular.monolith.rental.domain.Rental;
import com.example.library.modular.monolith.rental.domain.RentalNotFoundException;
import com.example.library.modular.monolith.rental.domain.RentalStatus;
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
        inventory.release(rental.bookId());
        return rentals.update(new Rental(rental.id(), rental.bookId(), rental.userId(), RentalStatus.RETURNED));
    }
}
