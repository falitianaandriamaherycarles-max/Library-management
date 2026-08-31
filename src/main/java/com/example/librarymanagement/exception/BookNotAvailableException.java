package com.example.librarymanagement.exception;

public class BookNotAvailableException extends RuntimeException {
    private final String bookId;

    public BookNotAvailableException(String bookId) {
        super("Book with ID " + bookId + " is currently not available");
        this.bookId = bookId;
    }

    public BookNotAvailableException(String bookId, String message) {
        super(message);
        this.bookId = bookId;
    }

    public String getBookId() {
        return bookId;
    }
}