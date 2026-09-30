package com.example.library.layered.monolith;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest(properties = "spring.docker.compose.lifecycle-management=none")
@AutoConfigureMockMvc
class LibraryApiIntegrationTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

    @Autowired
    MockMvc mockMvc;

    @Test
    void createsAndReadsBookThroughPostgres() throws Exception {
        var body = """
                {"title":"Domain-Driven Design","author":"Eric Evans","description":"Boundaries","totalCopies":3}
                """;

        var createResult = mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Domain-Driven Design"))
                .andReturn();

        var bookId = com.jayway.jsonpath.JsonPath.read(createResult.getResponse().getContentAsString(), "$.id");
        mockMvc.perform(get("/api/books/{bookId}", bookId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableCopies").value(3));
    }

    @Test
    void returnsProblemDetailWhenBookDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/books/{bookId}", Long.MAX_VALUE))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("BOOK_NOT_FOUND"));
    }

    @Test
    void rentsRejectsUnavailableCopiesAndReturnsBook() throws Exception {
        var body = """
                {"title":"Layered Architecture","author":"Example Author","description":"Layers","totalCopies":1}
                """;
        var bookResult = mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();
        var bookId = com.jayway.jsonpath.JsonPath.read(bookResult.getResponse().getContentAsString(), "$.id");

        var rentalResult = mockMvc.perform(post("/api/rentals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"bookId\":" + bookId + ",\"userId\":\"reader-1\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("RENTED"))
                .andReturn();

        mockMvc.perform(post("/api/rentals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"bookId\":" + bookId + ",\"userId\":\"reader-2\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("BOOK_UNAVAILABLE"));

        var rentalId = com.jayway.jsonpath.JsonPath.read(rentalResult.getResponse().getContentAsString(), "$.id");
        mockMvc.perform(post("/api/rentals/{rentalId}/return", rentalId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RETURNED"));

        mockMvc.perform(post("/api/rentals/{rentalId}/return", rentalId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RETURNED"));

        mockMvc.perform(get("/api/books/{bookId}", bookId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableCopies").value(1));
    }
}
