package com.example.library.hexagonal.monolith;

import com.example.library.hexagonal.monolith.book.application.port.out.BookRepository;
import com.example.library.hexagonal.monolith.book.application.port.out.ReviewRepository;
import com.example.library.hexagonal.monolith.book.adapter.out.persistence.BookPersistenceAdapter;
import com.example.library.hexagonal.monolith.book.adapter.out.persistence.ReviewPersistenceAdapter;
import com.example.library.hexagonal.monolith.rental.adapter.out.persistence.RentalPersistenceAdapter;
import com.example.library.hexagonal.monolith.rental.application.port.out.RentalRepository;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

@AnalyzeClasses(packages = "com.example.library.hexagonal.monolith")
class ArchitectureTest {

    @ArchTest
    static final ArchRule domain_does_not_depend_on_frameworks_or_adapters = noClasses()
            .that().resideInAnyPackage("..domain..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("org.springframework..", "..adapter..");

    @ArchTest
    static final ArchRule application_does_not_depend_on_adapters = noClasses()
            .that().resideInAnyPackage("..application..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("..adapter..");

    @ArchTest
    static final ArchRule inbound_adapters_do_not_depend_on_persistence = noClasses()
            .that().resideInAnyPackage("..adapter.in..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("..adapter.out..", "..persistence..");

    @ArchTest
    static final ArchRule book_and_rental_cores_use_declared_ports = noClasses()
            .that().resideInAnyPackage("..book.application..", "..rental.application..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("..adapter.in..", "..adapter.out..");

    @ArchTest
    static final ArchRule controllers_use_inbound_ports = classes()
            .that().haveSimpleNameEndingWith("Controller")
            .should().dependOnClassesThat()
            .resideInAnyPackage("..application.port.in..");

    @ArchTest
    static final ArchRule book_persistence_adapter_implements_port = classes()
            .that().haveSimpleName(BookPersistenceAdapter.class.getSimpleName())
            .should().implement(BookRepository.class);

    @ArchTest
    static final ArchRule review_persistence_adapter_implements_port = classes()
            .that().haveSimpleName(ReviewPersistenceAdapter.class.getSimpleName())
            .should().implement(ReviewRepository.class);

    @ArchTest
    static final ArchRule rental_persistence_adapter_implements_port = classes()
            .that().haveSimpleName(RentalPersistenceAdapter.class.getSimpleName())
            .should().implement(RentalRepository.class);

    @ArchTest
    static final ArchRule rental_core_does_not_bypass_book_port = noClasses()
            .that().resideInAnyPackage("..rental.application..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("..book.domain..", "..book.application.service..",
                    "..book.application.port.out..", "..book.adapter..");

    @ArchTest
    static final ArchRule book_core_does_not_bypass_rental_boundary = noClasses()
            .that().resideInAnyPackage("..book.application..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("..rental.domain..", "..rental.application..", "..rental.adapter..");
}
