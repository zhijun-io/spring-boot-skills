package com.example.library.rentalservice;

import com.example.library.rentalservice.rental.application.port.out.BookInventoryClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "spring.docker.compose.lifecycle-management=none")
@AutoConfigureMockMvc
@Import({TestcontainersConfiguration.class, LibraryApiIntegrationTest.StubCatalogConfiguration.class})
class LibraryApiIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void rentsAndReturnsBookThroughCatalogPort() throws Exception {
        var rentalResult = mockMvc.perform(post("/api/rentals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"bookId\":7,\"userId\":\"reader-1\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("RENTED"))
                .andReturn();

        var rentalId = com.jayway.jsonpath.JsonPath.read(rentalResult.getResponse().getContentAsString(), "$.id");
        mockMvc.perform(post("/api/rentals/{rentalId}/return", rentalId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RETURNED"));

        mockMvc.perform(post("/api/rentals/{rentalId}/return", rentalId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RETURNED"));
    }

    @Test
    void returnsStableProblemDetailForUnknownRental() throws Exception {
        mockMvc.perform(post("/api/rentals/{rentalId}/return", Long.MAX_VALUE))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.code").value("RENTAL_NOT_FOUND"));
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class StubCatalogConfiguration {

        @Bean
        @Primary
        BookInventoryClient bookInventoryClient() {
            return new BookInventoryClient() {
                @Override
                public void reserve(Long bookId) {
                }

                @Override
                public void release(Long bookId) {
                }
            };
        }
    }
}
