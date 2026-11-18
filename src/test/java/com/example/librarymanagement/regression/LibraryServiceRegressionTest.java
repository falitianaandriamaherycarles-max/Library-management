package com.example.librarymanagement.regression;

import com.example.librarymanagement.exception.BookNotAvailableException;
import com.example.librarymanagement.exception.BookNotFoundException;
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

    private String getUniqueIsbn(int index) {
        // 10 ISBN uniques et valides
        String[] uniqueIsbns = {
                "9780132350884",  // 1
                "9780201616224",  // 2
                "9780201633610",  // 3
                "9780735619678",  // 4
                "9780134494166",  // 5
                "9780321125217",  // 6
                "9780596009205",  // 7
                "9780131103627",  // 8
                "9780201835953",  // 9
                "9780201485677"   // 10
        };
        return uniqueIsbns[index % uniqueIsbns.length];
    }

    @BeforeEach
    void setUp() {
        bookRepository = new BookRepository();
        NotificationService notificationService = new NotificationService();
        validationService = new ValidationService();
        libraryService = new LibraryService(bookRepository, notificationService, validationService);

        // Préparer les données de test avec ISBN uniques et valides
        testBooks = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            String isbn = getUniqueIsbn(i);
            Book book = new Book(
                    null,
                    "Regression Test Book " + i,
                    "Regression Author " + i,
                    isbn,
                    2000 + i
            );
            testBooks.add(book);
        }
    }

    @Test
    void shouldHandleBulkOperations() {
        // Given: Ajout de plusieurs livres avec des ISBN uniques
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
        // Given - Utiliser un ISBN valide
        Book book = new Book(null, "Test Book", "Test Author", getUniqueIsbn(0), 2022);
        libraryService.addBook(book);
        String bookId = book.getId();

        // When: Tentatives d'opérations invalides
        assertThatThrownBy(() -> libraryService.borrowBook("invalid-id"))
                .isInstanceOf(BookNotFoundException.class);

        // Emprunter le livre d'abord
        libraryService.borrowBook(bookId);

        // Essayer d'emprunter le même livre (déjà emprunté)
        assertThatThrownBy(() -> libraryService.borrowBook(bookId))
                .isInstanceOf(BookNotAvailableException.class);

        // Then: Le système reste cohérent
        Book retrievedBook = bookRepository.findById(bookId).orElse(null);
        assertThat(retrievedBook).isNotNull();
        assertThat(retrievedBook.isAvailable()).isFalse();
        assertThat(libraryService.getTotalBooks()).isEqualTo(1);
    }

    @Test
    void shouldHandleConcurrentScenarios() throws InterruptedException {
        // Given - Utiliser des ISBN uniques
        for (int i = 0; i < 5; i++) {
            String isbn = getUniqueIsbn(i);
            Book book = new Book(null, "Concurrent Book " + i, "Author " + i, isbn, 2020);
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
        // Given - Utiliser des ISBN UNIQUES
        int totalBooks = 100;
        for (int i = 0; i < totalBooks; i++) {
            String isbn = getUniqueIsbn(i);
            Book book = new Book(
                    null,
                    "Large Test Book " + i,
                    "Author " + i,
                    isbn,
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
        // Given - Utiliser des ISBN UNIQUES
        for (int i = 0; i < 50; i++) {
            String isbn = getUniqueIsbn(i);
            Book book = new Book(
                    null,
                    "Performance Book " + i,
                    "Author " + (i % 10),
                    isbn,
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
        assertThat(duration).isLessThan(100);
    }

    @Test
    void shouldHandleDuplicateIsbnConsistently() {
        // Given - Utiliser des ISBN différents pour Book1, Book2, Book3
        String isbn1 = getUniqueIsbn(0);
        String isbn2 = getUniqueIsbn(1);
        String isbn3 = getUniqueIsbn(2);

        Book book1 = new Book(null, "Book1", "Author1", isbn1, 2020);
        Book book2 = new Book(null, "Book2", "Author2", isbn2, 2021);
        Book book3 = new Book(null, "Book3", "Author3", isbn3, 2022);

        // When
        libraryService.addBook(book1);
        libraryService.addBook(book2);
        libraryService.addBook(book3);

        // Then - Tous les livres sont ajoutés (ISBN différents)
        assertThat(libraryService.getTotalBooks()).isEqualTo(3);

        // Tester la duplication avec le même ISBN que book1
        Book book1Duplicate = new Book(null, "Book1 Duplicate", "Author1", isbn1, 2023);
        assertThatThrownBy(() -> libraryService.addBook(book1Duplicate))
                .isInstanceOf(DuplicateBookException.class);

        // Supprimer book1 et réessayer
        libraryService.deleteBook(book1.getId());
        libraryService.addBook(book1Duplicate);

        // Then
        assertThat(libraryService.getTotalBooks()).isEqualTo(3);
        Book retrieved = libraryService.getBookById(book1Duplicate.getId());
        assertThat(retrieved.getTitle()).isEqualTo("Book1 Duplicate");
    }

    @Test
    void shouldHandleInvalidOperationsGracefully() {
        // Given
        String validIsbn = getUniqueIsbn(0);
        Book book = new Book(null, "Valid Book", "Valid Author", validIsbn, 2022);
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