package com.example.library.modular.monolith;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.modulith.Modulithic;

@SpringBootApplication
@Modulithic(systemName = "library-modular-monolith", sharedModules = "shared")
public class LibraryModularMonolithApplication {

	public static void main(String[] args) {
		SpringApplication.run(LibraryModularMonolithApplication.class, args);
	}

}
