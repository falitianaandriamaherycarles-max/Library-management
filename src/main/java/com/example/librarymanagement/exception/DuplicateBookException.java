package com.example.librarymanagement.exception;

public class DuplicateBookException extends RuntimeException {
    private final String isbn;

    public DuplicateBookException(String isbn) {
        super("Book with ISBN " + isbn + " already exists in the library");
        this.isbn = isbn;
    }

    public DuplicateBookException(String isbn, String message) {
        super(message);
        this.isbn = isbn;
    }

    public String getIsbn() {
        return isbn;
    }
}