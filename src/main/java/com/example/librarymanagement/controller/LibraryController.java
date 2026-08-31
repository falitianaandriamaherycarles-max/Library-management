package com.example.librarymanagement.controller;

import com.example.librarymanagement.model.Book;
import com.example.librarymanagement.service.LibraryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class LibraryController {

    private final LibraryService libraryService;

    @GetMapping
    public ResponseEntity<List<Book>> getAllBooks() {
        return ResponseEntity.ok(libraryService.getAllAvailableBooks());
    }

    @GetMapping("/all")
    public ResponseEntity<List<Book>> getAllBooksIncludingBorrowed() {
        return ResponseEntity.ok(libraryService.searchBooks(""));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Book> getBookById(@PathVariable String id) {
        return ResponseEntity.ok(libraryService.getBookById(id));
    }

    @PostMapping
    public ResponseEntity<Book> addBook(@RequestBody Book book) {
        Book savedBook = libraryService.addBook(book);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedBook);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Book> updateBook(@PathVariable String id, @RequestBody Book book) {
        book.setId(id);
        libraryService.updateBook(book);
        return ResponseEntity.ok(libraryService.getBookById(id));
    }

    @PostMapping("/{id}/borrow")
    public ResponseEntity<Book> borrowBook(@PathVariable String id) {
        Book borrowedBook = libraryService.borrowBook(id);
        return ResponseEntity.ok(borrowedBook);
    }

    @PostMapping("/{id}/return")
    public ResponseEntity<Book> returnBook(@PathVariable String id) {
        Book returnedBook = libraryService.returnBook(id);
        return ResponseEntity.ok(returnedBook);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable String id) {
        libraryService.deleteBook(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<Book>> searchBooks(@RequestParam String keyword) {
        return ResponseEntity.ok(libraryService.searchBooks(keyword));
    }

    @GetMapping("/author/{author}")
    public ResponseEntity<List<Book>> getBooksByAuthor(@PathVariable String author) {
        return ResponseEntity.ok(libraryService.getBooksByAuthor(author));
    }

    @GetMapping("/stats")
    public ResponseEntity<StatsResponse> getStats() {
        StatsResponse stats = new StatsResponse(
                libraryService.getTotalBooks(),
                libraryService.getAvailableBooksCount(),
                libraryService.getTotalBooks() - libraryService.getAvailableBooksCount()
        );
        return ResponseEntity.ok(stats);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                e.getMessage(),
                System.currentTimeMillis()
        );
        return ResponseEntity.badRequest().body(error);
    }

    // Response classes
    public record StatsResponse(long total, long available, long borrowed) {}

    public record ErrorResponse(int status, String message, long timestamp) {}
}