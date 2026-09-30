package com.example.library.hexagonal.monolith;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(properties = "spring.docker.compose.lifecycle-management=none")
class LibraryHexagonalMonolithApplicationTests {

	@Test
	void contextLoads() {
	}

}
