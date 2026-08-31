package com.example.librarymanagement.regression;

import com.example.librarymanagement.exception.DuplicateBookException;
import com.example.librarymanagement.model.Book;
import com.example.librarymanagement.repository.BookRepository;
import com.example.librarymanagement.service.LibraryService;
import com.example.librarymanagement.service.NotificationService;
import com.example.librarymanagement.service.ValidationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LibraryServiceRegressionTest {

    private LibraryService libraryService;
    private BookRepository bookRepository;
    private ValidationService validationService;
    private List<Book> testBooks;

    @BeforeEach
    void setUp() {
        bookRepository = new BookRepository();
        NotificationService notificationService = new NotificationService();
        validationService = new ValidationService();
        libraryService = new LibraryService(bookRepository, notificationService, validationService);

        // Préparer les données de test
        testBooks = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Book book = new Book(
                    null,
                    "Regression Test Book " + i,
                    "Regression Author " + i,
                    "978-0-13-235088-" + String.format("%02d", i),
                    2000 + i
            );
            testBooks.add(book);
        }
    }

    @Test
    void shouldHandleBulkOperations() {
        // Given: Ajout de plusieurs livres
        for (Book book : testBooks) {
            libraryService.addBook(book);
        }

        // When: Recherche et emprunt de livres
        long initialCount = libraryService.getAvailableBooksCount();

        // Emprunter tous les livres pairs
        for (int i = 0; i < testBooks.size(); i += 2) {
            String bookId = testBooks.get(i).getId();
            libraryService.borrowBook(bookId);
        }

        // Then: Vérifier que les livres impairs sont toujours disponibles
        long afterBorrowCount = libraryService.getAvailableBooksCount();
        assertThat(afterBorrowCount).isEqualTo(initialCount - 5);

        // Vérifier que les livres pairs ne sont pas disponibles
        for (int i = 0; i < testBooks.size(); i += 2) {
            Book book = bookRepository.findById(testBooks.get(i).getId()).orElse(null);
            assertThat(book).isNotNull();
            assertThat(book.isAvailable()).isFalse();
        }
    }

    @Test
    void shouldMaintainConsistencyAfterErrorScenarios() {
        // Given
        Book book = new Book(null, "Test Book", "Test Author", "978-0-13-235088-99", 2022);
        libraryService.addBook(book);
        String bookId = book.getId();

        // When: Tentatives d'opérations invalides
        assertThatThrownBy(() -> libraryService.borrowBook("invalid-id"))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> libraryService.borrowBook(bookId))
                .isInstanceOf(IllegalStateException.class); // Déjà emprunté

        // Then: Le système reste cohérent
        Book retrievedBook = bookRepository.findById(bookId).orElse(null);
        assertThat(retrievedBook).isNotNull();
        assertThat(retrievedBook.isAvailable()).isTrue();
        assertThat(libraryService.getTotalBooks()).isEqualTo(1);
    }

    @Test
    void shouldHandleConcurrentScenarios() throws InterruptedException {
        // Given
        for (int i = 0; i < 5; i++) {
            Book book = new Book(null, "Concurrent Book " + i, "Author",
                    "978-0-13-235088-" + (100 + i), 2020);
            libraryService.addBook(book);
        }

        // When: Simulations d'accès concurrent
        ExecutorService executor = Executors.newFixedThreadPool(3);

        for (int i = 0; i < 3; i++) {
            executor.submit(() -> {
                List<Book> books = libraryService.getAllAvailableBooks();
                if (!books.isEmpty()) {
                    try {
                        libraryService.borrowBook(books.get(0).getId());
                    } catch (Exception e) {
                        // Ignorer les exceptions de concurrence
                    }
                }
            });
        }

        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        // Then: Le système reste cohérent
        assertThat(libraryService.getTotalBooks()).isEqualTo(5);
        assertThat(libraryService.getAvailableBooksCount()).isBetween(2L, 5L);
    }

    @Test
    void shouldHandleLargeNumberOfBooks() {
        // Given
        int totalBooks = 100;
        for (int i = 0; i < totalBooks; i++) {
            Book book = new Book(
                    null,
                    "Large Test Book " + i,
                    "Author " + i,
                    "978-0-13-235088-" + String.format("%03d", i),
                    2000 + (i % 20)
            );
            libraryService.addBook(book);
        }

        // When
        long count = libraryService.getTotalBooks();

        // Then
        assertThat(count).isEqualTo(totalBooks);

        // Emprunter la moitié des livres
        List<Book> availableBooks = libraryService.getAllAvailableBooks();
        int toBorrow = availableBooks.size() / 2;
        for (int i = 0; i < toBorrow; i++) {
            libraryService.borrowBook(availableBooks.get(i).getId());
        }

        // Then
        assertThat(libraryService.getAvailableBooksCount()).isEqualTo(totalBooks - toBorrow);
        assertThat(libraryService.getTotalBooks()).isEqualTo(totalBooks);
    }

    @Test
    void shouldHandleSearchPerformance() {
        // Given
        for (int i = 0; i < 50; i++) {
            Book book = new Book(
                    null,
                    "Performance Book " + i,
                    "Author " + (i % 10),
                    "978-0-13-235088-" + String.format("%03d", i),
                    2000 + (i % 20)
            );
            libraryService.addBook(book);
        }

        // When
        long startTime = System.currentTimeMillis();
        List<Book> results = libraryService.searchBooks("Book");
        long duration = System.currentTimeMillis() - startTime;

        // Then
        assertThat(results).hasSize(50);
        assertThat(duration).isLessThan(100); // Moins de 100ms
    }

    @Test
    void shouldHandleDuplicateIsbnConsistently() {
        // Given
        Book book1 = new Book(null, "Book1", "Author1", "DUPLICATE-ISBN", 2020);
        Book book2 = new Book(null, "Book2", "Author2", "DUPLICATE-ISBN", 2021);
        Book book3 = new Book(null, "Book3", "Author3", "DUPLICATE-ISBN", 2022);

        // When
        libraryService.addBook(book1);

        // Then
        assertThatThrownBy(() -> libraryService.addBook(book2))
                .isInstanceOf(DuplicateBookException.class);

        // Le système reste cohérent
        assertThat(libraryService.getTotalBooks()).isEqualTo(1);

        // Supprimer le livre et réessayer
        libraryService.deleteBook(book1.getId());
        libraryService.addBook(book2);

        // Then
        assertThat(libraryService.getTotalBooks()).isEqualTo(1);
        Book retrieved = libraryService.getBookById(book2.getId());
        assertThat(retrieved.getTitle()).isEqualTo("Book2");
    }

    @Test
    void shouldHandleInvalidOperationsGracefully() {
        // Given
        Book book = new Book(null, "Valid Book", "Valid Author", "978-0132350884", 2022);
        libraryService.addBook(book);

        // When/Then - Tentatives d'opérations invalides
        assertThatThrownBy(() -> libraryService.borrowBook(null))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> libraryService.borrowBook(""))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> libraryService.returnBook(null))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> libraryService.deleteBook(null))
                .isInstanceOf(IllegalArgumentException.class);

        // Le système reste intact
        assertThat(libraryService.getTotalBooks()).isEqualTo(1);
        assertThat(libraryService.getAvailableBooksCount()).isEqualTo(1);
    }
}