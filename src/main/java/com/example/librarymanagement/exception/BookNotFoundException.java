package com.example.librarymanagement.exception;

public class BookNotFoundException extends RuntimeException {
    private final String bookId;

    public BookNotFoundException(String bookId) {
        super("Book not found with ID: " + bookId);
        this.bookId = bookId;
    }

    public BookNotFoundException(String bookId, String message) {
        super(message);
        this.bookId = bookId;
    }

    public String getBookId() {
        return bookId;
    }
}