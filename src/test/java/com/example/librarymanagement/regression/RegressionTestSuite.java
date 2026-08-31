package com.example.librarymanagement.regression;

import com.example.librarymanagement.integration.LibraryServiceIntegrationTest;
import com.example.librarymanagement.integration.RepositoryIntegrationTest;
import com.example.librarymanagement.unit.service.LibraryServiceTest;
import com.example.librarymanagement.unit.service.ValidationServiceTest;
import com.example.librarymanagement.unit.repository.BookRepositoryTest;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

@Suite
@SuiteDisplayName("Regression Test Suite")
@SelectClasses({
        // Unit Tests
        LibraryServiceTest.class,
        ValidationServiceTest.class,
        BookRepositoryTest.class,

        // Integration Tests
        LibraryServiceIntegrationTest.class,
        RepositoryIntegrationTest.class,

        // Regression Tests
        LibraryServiceRegressionTest.class
})
public class RegressionTestSuite {
    // Cette classe sert de point d'entrée pour la suite de tests de régression
}