package com.example.librarymanagement.service;

import com.example.librarymanagement.model.Book;
import com.example.librarymanagement.util.ISBNValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Year;

@Slf4j
@Service
public class ValidationService {

    public void validateBook(Book book) {
        if (book == null) {
            throw new IllegalArgumentException("Book cannot be null");
        }

        validateTitle(book.getTitle());
        validateAuthor(book.getAuthor());
        validateISBN(book.getIsbn());
        validatePublicationYear(book.getPublicationYear());

        log.debug("✅ Book validation passed for: {}", book.getTitle());
    }

    private void validateTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Book title cannot be empty");
        }
        if (title.length() > 200) {
            throw new IllegalArgumentException("Book title cannot exceed 200 characters");
        }
        if (!title.matches("^[\\p{L}\\p{N}\\s\\p{Punct}]+$")) {
            throw new IllegalArgumentException("Book title contains invalid characters");
        }
    }

    private void validateAuthor(String author) {
        if (author == null || author.trim().isEmpty()) {
            throw new IllegalArgumentException("Book author cannot be empty");
        }
        if (author.length() > 100) {
            throw new IllegalArgumentException("Book author cannot exceed 100 characters");
        }
        if (!author.matches("^[\\p{L}\\s\\-']+$")) {
            throw new IllegalArgumentException("Book author contains invalid characters");
        }
    }

    private void validateISBN(String isbn) {
        if (isbn == null || isbn.trim().isEmpty()) {
            throw new IllegalArgumentException("ISBN cannot be empty");
        }
        if (!ISBNValidator.isValidISBN(isbn)) {
            throw new IllegalArgumentException("Invalid ISBN format: " + isbn);
        }
    }

    private void validatePublicationYear(int year) {
        int currentYear = Year.now().getValue();
        if (year < 1450) {
            throw new IllegalArgumentException("Publication year must be at least 1450");
        }
        if (year > currentYear + 1) {
            throw new IllegalArgumentException("Publication year cannot be in the distant future");
        }
    }

    public void validateBookId(String bookId) {
        if (bookId == null || bookId.trim().isEmpty()) {
            throw new IllegalArgumentException("Book ID cannot be empty");
        }
        if (!bookId.matches("^[a-zA-Z0-9\\-]+$")) {
            throw new IllegalArgumentException("Book ID contains invalid characters");
        }
    }
}