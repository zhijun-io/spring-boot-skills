package com.example.library.catalog;

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
    void managesBookInventoryThroughInternalContract() throws Exception {
        var body = """
                {"title":"Building Evolutionary Architectures","author":"Rebecca Parsons","description":"Architecture fitness","totalCopies":1}
                """;

        var bookResult = mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.availableCopies").value(1))
                .andReturn();

        var bookId = com.jayway.jsonpath.JsonPath.read(bookResult.getResponse().getContentAsString(), "$.id");
        mockMvc.perform(post("/internal/books/{bookId}/reserve", bookId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/books/{bookId}", bookId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableCopies").value(0));

        mockMvc.perform(post("/internal/books/{bookId}/reserve", bookId))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.code").value("BOOK_UNAVAILABLE"));

        mockMvc.perform(post("/internal/books/{bookId}/release", bookId))
                .andExpect(status().isNoContent());
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
