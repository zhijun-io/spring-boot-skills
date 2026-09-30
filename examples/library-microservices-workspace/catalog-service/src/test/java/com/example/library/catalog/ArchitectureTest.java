package com.example.library.catalog;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "com.example.library.catalog")
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
    static final ArchRule book_core_uses_declared_ports = noClasses()
            .that().resideInAnyPackage("..book.application..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("..adapter.in..", "..adapter.out..");

}
