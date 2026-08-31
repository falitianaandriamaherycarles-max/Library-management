package com.example.librarymanagement.unit.service;

import com.example.librarymanagement.model.Book;
import com.example.librarymanagement.service.ValidationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ValidationServiceTest {

    private ValidationService validationService;
    private Book validBook;

    @BeforeEach
    void setUp() {
        validationService = new ValidationService();
        validBook = new Book("1", "Clean Code", "Robert Martin", "978-0132350884", 2008);
    }

    @Test
    void shouldValidateValidBook() {
        assertThatCode(() -> validationService.validateBook(validBook))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldThrowExceptionWhenBookIsNull() {
        assertThatThrownBy(() -> validationService.validateBook(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot be null");
    }

    @Test
    void shouldThrowExceptionWhenTitleIsNull() {
        validBook.setTitle(null);
        assertThatThrownBy(() -> validationService.validateBook(validBook))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("title cannot be empty");
    }

    @Test
    void shouldThrowExceptionWhenTitleIsEmpty() {
        validBook.setTitle("");
        assertThatThrownBy(() -> validationService.validateBook(validBook))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("title cannot be empty");
    }

    @Test
    void shouldThrowExceptionWhenAuthorIsNull() {
        validBook.setAuthor(null);
        assertThatThrownBy(() -> validationService.validateBook(validBook))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("author cannot be empty");
    }

    @Test
    void shouldThrowExceptionWhenAuthorIsEmpty() {
        validBook.setAuthor("");
        assertThatThrownBy(() -> validationService.validateBook(validBook))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("author cannot be empty");
    }

    @Test
    void shouldThrowExceptionWhenIsbnIsNull() {
        validBook.setIsbn(null);
        assertThatThrownBy(() -> validationService.validateBook(validBook))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ISBN cannot be empty");
    }

    @Test
    void shouldThrowExceptionWhenIsbnIsInvalid() {
        validBook.setIsbn("invalid-isbn");
        assertThatThrownBy(() -> validationService.validateBook(validBook))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid ISBN");
    }

    @ParameterizedTest
    @ValueSource(strings = {"978-0132350884", "9780132350884", "0-13-235088-2", "0132350882"})
    void shouldAcceptValidIsbnFormats(String isbn) {
        validBook.setIsbn(isbn);
        assertThatCode(() -> validationService.validateBook(validBook))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldThrowExceptionWhenYearIsTooOld() {
        validBook.setPublicationYear(1400);
        assertThatThrownBy(() -> validationService.validateBook(validBook))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("at least 1450");
    }

    @Test
    void shouldThrowExceptionWhenYearIsTooFarInFuture() {
        validBook.setPublicationYear(2100);
        assertThatThrownBy(() -> validationService.validateBook(validBook))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("future");
    }

    @Test
    void shouldValidateBookId() {
        assertThatCode(() -> validationService.validateBookId("123-456-789"))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldThrowExceptionWhenBookIdIsNull() {
        assertThatThrownBy(() -> validationService.validateBookId(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot be empty");
    }

    @Test
    void shouldThrowExceptionWhenBookIdHasInvalidCharacters() {
        assertThatThrownBy(() -> validationService.validateBookId("invalid@id!"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("invalid characters");
    }
}