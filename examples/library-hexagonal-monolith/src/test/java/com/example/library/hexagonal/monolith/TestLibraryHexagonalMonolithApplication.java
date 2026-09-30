package com.example.library.hexagonal.monolith;

import org.springframework.boot.SpringApplication;

public class TestLibraryHexagonalMonolithApplication {

	public static void main(String[] args) {
		SpringApplication.from(LibraryHexagonalMonolithApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
