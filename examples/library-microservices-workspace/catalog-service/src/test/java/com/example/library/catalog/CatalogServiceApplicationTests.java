package com.example.library.catalog;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(properties = "spring.docker.compose.lifecycle-management=none")
class CatalogServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}
