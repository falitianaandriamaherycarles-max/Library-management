package com.example.librarymanagement.integration;

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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class LibraryServiceIntegrationTest {

    private BookRepository bookRepository;
    private NotificationService notificationService;
    private ValidationService validationService;
    private LibraryService libraryService;

    @BeforeEach
    void setUp() {
        bookRepository = new BookRepository();
        notificationService = new NotificationService();
        validationService = new ValidationService();
        libraryService = new LibraryService (bookRepository, notificationService, validationService );
    }

    @Test
    void shouldAddAndRetrieveBook() {
        // Given
        Book book = new Book(null, "Test Book", "Test Author", "978-0132350884", 2022);

        // When
        Book savedBook = libraryService.addBook(book);
        Book retrievedBook = libraryService.getBookById(savedBook.getId());

        // Then
        assertThat(retrievedBook).isNotNull();
        assertThat(retrievedBook.getTitle()).isEqualTo("Test Book");
        assertThat(retrievedBook.getId()).isEqualTo(savedBook.getId());
        assertThat(retrievedBook.isAvailable()).isTrue();
    }

    @Test
    void shouldNotAddDuplicateBook() {
        // Given
        Book book = new Book(null, "Test Book", "Test Author", "978-0132350884", 2022);
        libraryService.addBook(book);

        // When/Then
        assertThatThrownBy(() -> libraryService.addBook(book))
                .isInstanceOf(DuplicateBookException.class);
    }

    @Test
    void shouldBorrowAndReturnBook() {
        // Given
        Book book = new Book(null, "Test Book", "Test Author", "978-0132350884", 2022);
        Book savedBook = libraryService.addBook(book);

        // When - Borrow
        Book borrowedBook = libraryService.borrowBook(savedBook.getId());

        // Then - Borrowed
        assertThat(borrowedBook.isAvailable()).isFalse();
        assertThat(bookRepository.findById(savedBook.getId()).get().isAvailable()).isFalse();

        // When - Return
        Book returnedBook = libraryService.returnBook(savedBook.getId());

        // Then - Returned
        assertThat(returnedBook.isAvailable()).isTrue();
        assertThat(bookRepository.findById(savedBook.getId()).get().isAvailable()).isTrue();
    }

    @Test
    void shouldThrowExceptionWhenBorrowingNonExistentBook() {
        assertThatThrownBy(() -> libraryService.borrowBook("non-existent-id"))
                .isInstanceOf(BookNotFoundException.class);
    }

    @Test
    void shouldThrowExceptionWhenBorrowingAlreadyBorrowedBook() {
        // Given
        Book book = new Book(null, "Test Book", "Test Author", "978-0132350884", 2022);
        Book savedBook = libraryService.addBook(book);
        libraryService.borrowBook(savedBook.getId());

        // When/Then
        assertThatThrownBy(() -> libraryService.borrowBook(savedBook.getId()))
                .isInstanceOf(BookNotAvailableException.class);
    }

    @Test
    void shouldSearchBooks() {
        // Given
        Book book1 = new Book(null, "Clean Code", "Robert Martin", "978-0132350884", 2008);
        Book book2 = new Book(null, "The Pragmatic Programmer", "Andrew Hunt", "978-0201616224", 1999);
        Book book3 = new Book(null, "Design Patterns", "Erich Gamma", "978-0201633610", 1994);

        libraryService.addBook(book1);
        libraryService.addBook(book2);
        libraryService.addBook(book3);

        // When
        List<Book> results = libraryService.searchBooks("Code");

        // Then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getTitle()).contains("Code");
    }

    @Test
    void shouldGetBooksByAuthor() {
        // Given
        Book book1 = new Book(null, "Clean Code", "Robert Martin", "978-0132350884", 2008);
        Book book2 = new Book(null, "Clean Architecture", "Robert Martin", "978-0134494166", 2017);
        Book book3 = new Book(null, "Code Complete", "Steve McConnell", "978-0735619678", 2004);

        libraryService.addBook(book1);
        libraryService.addBook(book2);
        libraryService.addBook(book3);

        // When
        List<Book> martinBooks = libraryService.getBooksByAuthor("Robert Martin");

        // Then
        assertThat(martinBooks).hasSize(2);
        assertThat(martinBooks).allMatch(book -> book.getAuthor().equals("Robert Martin"));
    }

    @Test
    void shouldMaintainConsistencyAfterMultipleOperations() {
        // Given
        for (int i = 0; i < 5; i++) {
            Book book = new Book(null, "Book " + i, "Author " + i, "978-013235088" + i, 2000 + i);
            libraryService.addBook(book);
        }

        // When
        long initialCount = libraryService.getAvailableBooksCount();

        // Borrow 2 books
        List<Book> availableBooks = libraryService.getAllAvailableBooks();
        for (int i = 0; i < 2 && i < availableBooks.size(); i++) {
            libraryService.borrowBook(availableBooks.get(i).getId());
        }

        // Then
        assertThat(libraryService.getAvailableBooksCount()).isEqualTo(initialCount - 2);
        assertThat(libraryService.getTotalBooks()).isEqualTo(5);

        // Return one book
        Book borrowedBook = availableBooks.get(0);
        libraryService.returnBook(borrowedBook.getId());

        // Then
        assertThat(libraryService.getAvailableBooksCount()).isEqualTo(initialCount - 1);
    }

    @Test
    void shouldDeleteBookSuccessfully() {
        // Given
        Book book = new Book(null, "Test Book", "Test Author", "978-0132350884", 2022);
        Book savedBook = libraryService.addBook(book);

        // When
        boolean deleted = libraryService.deleteBook(savedBook.getId());

        // Then
        assertThat(deleted).isTrue();
        assertThatThrownBy(() -> libraryService.getBookById(savedBook.getId()))
                .isInstanceOf(BookNotFoundException.class);
    }
}