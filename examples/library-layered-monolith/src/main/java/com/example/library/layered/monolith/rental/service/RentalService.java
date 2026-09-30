package com.example.library.layered.monolith.rental.service;

import com.example.library.layered.monolith.rental.domain.Rental;
import com.example.library.layered.monolith.book.service.BookInventory;
import com.example.library.layered.monolith.rental.domain.RentalNotFoundException;
import com.example.library.layered.monolith.rental.domain.RentalStatus;
import com.example.library.layered.monolith.rental.persistence.RentalPersistenceMapper;
import com.example.library.layered.monolith.rental.persistence.RentalMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RentalService {

    private final RentalMapper rentalMapper;
    private final RentalPersistenceMapper entityMapper;
    private final BookInventory inventory;

    @Transactional
    public Rental rent(Rental rental) {
        inventory.reserve(rental.bookId());
        var entity = entityMapper.toEntity(rental);
        rentalMapper.insert(entity);
        return entityMapper.toDomain(entity);
    }

    @Transactional
    public Rental returnRental(Long rentalId) {
        var rental = findById(rentalId);
        if (rental.status() == RentalStatus.RETURNED) {
            return rental;
        }
        if (rentalMapper.markReturned(rentalId) == 0) {
            var current = findById(rentalId);
            if (current.status() == RentalStatus.RETURNED) {
                return current;
            }
            throw new IllegalStateException("Rental could not be returned");
        }
        inventory.release(rental.bookId());
        return findById(rentalId);
    }

    private Rental findById(Long rentalId) {
        var entity = rentalMapper.selectById(rentalId);
        if (entity == null) {
            throw new RentalNotFoundException(rentalId);
        }
        return entityMapper.toDomain(entity);
    }
}
