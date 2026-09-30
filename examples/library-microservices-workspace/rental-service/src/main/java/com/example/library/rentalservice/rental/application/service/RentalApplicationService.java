package com.example.library.rentalservice.rental.application.service;

import com.example.library.rentalservice.rental.application.port.in.RentalUseCase;
import com.example.library.rentalservice.rental.application.port.out.BookInventoryClient;
import com.example.library.rentalservice.rental.application.port.out.RentalRepository;
import com.example.library.rentalservice.rental.domain.Rental;
import com.example.library.rentalservice.rental.domain.RentalNotFoundException;
import com.example.library.rentalservice.rental.domain.RentalStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RentalApplicationService implements RentalUseCase {

    private final BookInventoryClient inventory;
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
