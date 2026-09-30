package com.example.library.rentalservice;

import com.example.library.rentalservice.rental.adapter.out.catalog.CatalogInventoryHttpAdapter;
import com.example.library.rentalservice.rental.domain.BookUnavailableException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class CatalogInventoryHttpAdapterTest {

    @Test
    void sendsReserveRequestToCatalogContract() {
        var builder = RestClient.builder();
        var server = MockRestServiceServer.bindTo(builder).build();
        var adapter = new CatalogInventoryHttpAdapter(builder, "http://catalog-service");
        server.expect(requestTo("http://catalog-service/internal/books/7/reserve"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess());

        adapter.reserve(7L);

        server.verify();
    }

    @Test
    void mapsCatalogConflictToStableApplicationFailure() {
        var builder = RestClient.builder();
        var server = MockRestServiceServer.bindTo(builder).build();
        var adapter = new CatalogInventoryHttpAdapter(builder, "http://catalog-service");
        server.expect(requestTo("http://catalog-service/internal/books/7/reserve"))
                .andRespond(withStatus(HttpStatus.CONFLICT));

        assertThatThrownBy(() -> adapter.reserve(7L))
                .isInstanceOf(BookUnavailableException.class);

        server.verify();
    }
}
