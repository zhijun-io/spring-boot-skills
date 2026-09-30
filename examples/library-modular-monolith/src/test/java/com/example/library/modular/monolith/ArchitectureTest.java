package com.example.library.modular.monolith;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "com.example.library.modular.monolith")
class ArchitectureTest {

    @Test
    void verifiesSpringModulithBoundaries() {
        ApplicationModules.of(LibraryModularMonolithApplication.class).verify();
    }

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
    static final ArchRule book_module_does_not_depend_on_rental_module = noClasses()
            .that().resideInAnyPackage("..book..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("..rental..");

    @ArchTest
    static final ArchRule rental_module_uses_only_book_public_api = noClasses()
            .that().resideInAnyPackage("..rental..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("..book.domain..", "..book.application..", "..book.adapter..");
}
