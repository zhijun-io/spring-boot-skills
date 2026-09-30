package com.example.library.modular.monolith;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "spring.docker.compose.lifecycle-management=none")
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class LibraryApiIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void createsReadsRentsAndReturnsBookThroughPorts() throws Exception {
        var body = """
                {"title":"Modular Monoliths","author":"Simon Brown","description":"Explicit module boundaries","totalCopies":1}
                """;

        var bookResult = mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.availableCopies").value(1))
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
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.code").value("BOOK_UNAVAILABLE"));

        var rentalId = com.jayway.jsonpath.JsonPath.read(rentalResult.getResponse().getContentAsString(), "$.id");
        mockMvc.perform(post("/api/rentals/{rentalId}/return", rentalId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RETURNED"));

        mockMvc.perform(get("/api/books/{bookId}", bookId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableCopies").value(1));
    }

    @Test
    void returnsStableProblemDetailForMalformedRequest() throws Exception {
        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("not-json"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.code").value("MALFORMED_REQUEST"));
    }
}
