package com.example.library.layered.monolith;

import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "com.example.library.layered.monolith")
class ArchitectureTest {

    @ArchTest
    static final ArchRule domain_does_not_depend_on_adapters = noClasses()
            .that().resideInAnyPackage("..domain..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("..web..", "..persistence..");

    @ArchTest
    static final ArchRule api_does_not_expose_persistence = noClasses()
            .that().resideInAnyPackage("..api..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("..persistence..");

    @ArchTest
    static final ArchRule web_does_not_depend_on_persistence = noClasses()
            .that().resideInAnyPackage("..web..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("..persistence..");

    @ArchTest
    static final ArchRule services_do_not_depend_on_web = noClasses()
            .that().resideInAnyPackage("..service..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("..web..");

    @ArchTest
    static final ArchRule persistence_does_not_depend_on_api = noClasses()
            .that().resideInAnyPackage("..persistence..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("..api..", "..web..");

    @ArchTest
    static final ArchRule book_web_does_not_depend_on_rental_persistence = noClasses()
            .that().resideInAnyPackage("..book.web..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("..rental.persistence..");

    @ArchTest
    static final ArchRule rental_web_does_not_depend_on_book_persistence = noClasses()
            .that().resideInAnyPackage("..rental.web..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("..book.persistence..");

    @ArchTest
    static final ArchRule rental_service_uses_book_application_api = noClasses()
            .that().resideInAnyPackage("..rental.service..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("..book.domain..", "..book.persistence..", "..book.web..");

    @ArchTest
    static final ArchRule shared_does_not_depend_on_business_features = noClasses()
            .that().resideInAnyPackage("..shared..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("..book..", "..rental..");
}
