package com.example.librarymanagement.service;

import com.example.librarymanagement.exception.BookNotAvailableException;
import com.example.librarymanagement.exception.BookNotFoundException;
import com.example.librarymanagement.exception.DuplicateBookException;
import com.example.librarymanagement.model.Book;
import com.example.librarymanagement.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LibraryService {

    private final BookRepository bookRepository;
    private final NotificationService notificationService;
    private final ValidationService validationService;

    public Book addBook(Book book) {
        log.info("Adding book: {}", book.getTitle());

        validationService.validateBook(book);

        Optional<Book> existingBook = bookRepository.findByIsbn(book.getIsbn());
        if (existingBook.isPresent()) {
            throw new DuplicateBookException(book.getIsbn());
        }

        Book savedBook = bookRepository.save(book);
        notificationService.notifyNewBookAdded(savedBook);
        log.info("Book added successfully: {}", savedBook.getTitle());
        return savedBook;
    }

    public Book borrowBook(String bookId) {
        log.info("Borrowing book with ID: {}", bookId);

        Book book = findBookOrThrow(bookId);

        if (!book.isAvailable()) {
            throw new BookNotAvailableException(bookId);
        }

        book.setAvailable(false);
        Book updatedBook = bookRepository.save(book);
        notificationService.notifyBookBorrowed(updatedBook);
        log.info("Book borrowed: {}", updatedBook.getTitle());
        return updatedBook;
    }

    public Book returnBook(String bookId) {
        log.info("↩️ Returning book with ID: {}", bookId);

        Book book = findBookOrThrow(bookId);

        if (book.isAvailable()) {
            throw new IllegalStateException("Book is already available");
        }

        book.setAvailable(true);
        Book updatedBook = bookRepository.save(book);
        notificationService.notifyBookReturned(updatedBook);
        log.info("Book returned: {}", updatedBook.getTitle());
        return updatedBook;
    }

    public List<Book> searchBooks(String keyword) {
        log.info("Searching books with keyword: {}", keyword);

        if (keyword == null || keyword.trim().isEmpty()) {
            return bookRepository.findAll();
        }

        String searchTerm = keyword.toLowerCase().trim();
        return bookRepository.findAll().stream()
                .filter(book ->
                        book.getTitle().toLowerCase().contains(searchTerm) ||
                                book.getAuthor().toLowerCase().contains(searchTerm) ||
                                book.getIsbn().contains(searchTerm)
                )
                .collect(Collectors.toList());
    }

    public List<Book> getAllAvailableBooks() {
        return bookRepository.findAll().stream()
                .filter(Book::isAvailable)
                .collect(Collectors.toList());
    }

    public List<Book> getBooksByAuthor(String author) {
        if (author == null || author.trim().isEmpty()) {
            return List.of();
        }
        return bookRepository.findAll().stream()
                .filter(book -> book.getAuthor().equalsIgnoreCase(author.trim()))
                .collect(Collectors.toList());
    }

    public boolean deleteBook(String bookId) {
        log.info("Deleting book with ID: {}", bookId);
        validationService.validateBookId(bookId);
        boolean deleted = bookRepository.delete(bookId);
        if (deleted) {
            log.info("Book deleted: {}", bookId);
        } else {
            log.warn("Book not found for deletion: {}", bookId);
        }
        return deleted;
    }

    public long getTotalBooks() {
        return bookRepository.count();
    }

    public long getAvailableBooksCount() {
        return getAllAvailableBooks().size();
    }

    public Book getBookById(String bookId) {
        return findBookOrThrow(bookId);
    }

    public void updateBook(Book book) {
        log.info("Updating book: {}", book.getId());
        validationService.validateBook(book);

        if (!bookRepository.exists(book.getId())) {
            throw new BookNotFoundException(book.getId());
        }

        bookRepository.save(book);
        log.info("Book updated: {}", book.getTitle());
    }

    private Book findBookOrThrow(String bookId) {
        validationService.validateBookId(bookId);
        return bookRepository.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException(bookId));
    }
}