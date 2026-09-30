package com.example.library.rentalservice.rental.domain;

import com.example.library.rentalservice.shared.error.ApplicationException;

public class CatalogUnavailableException extends ApplicationException {

    public CatalogUnavailableException() {
        super(503, "CATALOG_UNAVAILABLE", "catalog.unavailable");
    }
}
