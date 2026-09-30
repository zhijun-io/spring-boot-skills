package com.example.library.modular.monolith;

import org.springframework.boot.SpringApplication;

public class TestLibraryModularMonolithApplication {

	public static void main(String[] args) {
		SpringApplication.from(LibraryModularMonolithApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
