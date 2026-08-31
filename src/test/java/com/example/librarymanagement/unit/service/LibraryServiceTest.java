package com.example.librarymanagement.unit.service;

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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LibraryServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private ValidationService validationService;

    @InjectMocks
    private LibraryService libraryService;

    private Book testBook;

    @BeforeEach
    void setUp() {
        testBook = new Book("1", "Clean Code", "Robert Martin", "978-0132350884", 2008);
        testBook.setAvailable(true);
    }

    @Test
    void shouldAddBookSuccessfully() {
        // Given
        doNothing().when(validationService).validateBook(testBook);
        when(bookRepository.findByIsbn(testBook.getIsbn())).thenReturn(Optional.empty());
        when(bookRepository.save(any(Book.class))).thenReturn(testBook);

        // When
        Book addedBook = libraryService.addBook(testBook);

        // Then
        assertThat(addedBook).isNotNull();
        assertThat(addedBook.getTitle()).isEqualTo("Clean Code");
        verify(bookRepository, times(1)).save(testBook);
        verify(notificationService, times(1)).notifyNewBookAdded(testBook);
    }

    @Test
    void shouldThrowExceptionWhenAddingDuplicateBook() {
        // Given
        doNothing().when(validationService).validateBook(testBook);
        when(bookRepository.findByIsbn(testBook.getIsbn())).thenReturn(Optional.of(testBook));

        // When/Then
        assertThatThrownBy(() -> libraryService.addBook(testBook))
                .isInstanceOf(DuplicateBookException.class)
                .hasMessageContaining("already exists");

        verify(bookRepository, never()).save(any(Book.class));
        verify(notificationService, never()).notifyNewBookAdded(any(Book.class));
    }

    @Test
    void shouldBorrowBookSuccessfully() {
        // Given
        doNothing().when(validationService).validateBookId("1");
        when(bookRepository.findById("1")).thenReturn(Optional.of(testBook));
        when(bookRepository.save(any(Book.class))).thenReturn(testBook);

        // When
        Book borrowedBook = libraryService.borrowBook("1");

        // Then
        assertThat(borrowedBook).isNotNull();
        assertThat(borrowedBook.isAvailable()).isFalse();
        verify(notificationService, times(1)).notifyBookBorrowed(testBook);
    }

    @Test
    void shouldThrowExceptionWhenBorrowingUnavailableBook() {
        // Given
        testBook.setAvailable(false);
        doNothing().when(validationService).validateBookId("1");
        when(bookRepository.findById("1")).thenReturn(Optional.of(testBook));

        // When/Then
        assertThatThrownBy(() -> libraryService.borrowBook("1"))
                .isInstanceOf(BookNotAvailableException.class)
                .hasMessageContaining("not available");
    }

    @Test
    void shouldThrowExceptionWhenBookNotFound() {
        // Given
        doNothing().when(validationService).validateBookId("999");
        when(bookRepository.findById("999")).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> libraryService.borrowBook("999"))
                .isInstanceOf(BookNotFoundException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void shouldReturnBookSuccessfully() {
        // Given
        testBook.setAvailable(false);
        doNothing().when(validationService).validateBookId("1");
        when(bookRepository.findById("1")).thenReturn(Optional.of(testBook));
        when(bookRepository.save(any(Book.class))).thenReturn(testBook);

        // When
        Book returnedBook = libraryService.returnBook("1");

        // Then
        assertThat(returnedBook).isNotNull();
        assertThat(returnedBook.isAvailable()).isTrue();
        verify(notificationService, times(1)).notifyBookReturned(testBook);
    }

    @Test
    void shouldSearchBooksByKeyword() {
        // Given
        Book book1 = new Book("1", "Clean Code", "Robert Martin", "ISBN1", 2008);
        Book book2 = new Book("2", "Code Complete", "Steve McConnell", "ISBN2", 2004);
        Book book3 = new Book("3", "The Pragmatic Programmer", "Andrew Hunt", "ISBN3", 1999);

        when(bookRepository.findAll()).thenReturn(Arrays.asList(book1, book2, book3));

        // When
        List<Book> results = libraryService.searchBooks("Code");

        // Then
        assertThat(results).hasSize(2);
        assertThat(results).extracting(Book::getTitle)
                .containsExactlyInAnyOrder("Clean Code", "Code Complete");
    }

    @Test
    void shouldGetAllAvailableBooks() {
        // Given
        Book book1 = new Book("1", "Book1", "Author1", "ISBN1", 2000);
        book1.setAvailable(true);
        Book book2 = new Book("2", "Book2", "Author2", "ISBN2", 2001);
        book2.setAvailable(false);
        Book book3 = new Book("3", "Book3", "Author3", "ISBN3", 2002);
        book3.setAvailable(true);

        when(bookRepository.findAll()).thenReturn(Arrays.asList(book1, book2, book3));

        // When
        List<Book> availableBooks = libraryService.getAllAvailableBooks();

        // Then
        assertThat(availableBooks).hasSize(2);
        assertThat(availableBooks).allMatch(Book::isAvailable);
    }

    @Test
    void shouldGetBooksByAuthor() {
        // Given
        Book book1 = new Book("1", "Clean Code", "Robert Martin", "ISBN1", 2008);
        Book book2 = new Book("2", "Clean Architecture", "Robert Martin", "ISBN2", 2017);
        Book book3 = new Book("3", "Code Complete", "Steve McConnell", "ISBN3", 2004);

        when(bookRepository.findAll()).thenReturn(Arrays.asList(book1, book2, book3));

        // When
        List<Book> martinBooks = libraryService.getBooksByAuthor("Robert Martin");

        // Then
        assertThat(martinBooks).hasSize(2);
        assertThat(martinBooks).allMatch(book -> book.getAuthor().equals("Robert Martin"));
    }

    @Test
    void shouldDeleteBookSuccessfully() {
        // Given
        doNothing().when(validationService).validateBookId("1");
        when(bookRepository.delete("1")).thenReturn(true);

        // When
        boolean deleted = libraryService.deleteBook("1");

        // Then
        assertThat(deleted).isTrue();
        verify(bookRepository, times(1)).delete("1");
    }

    @Test
    void shouldGetTotalBooksCount() {
        // Given
        when(bookRepository.count()).thenReturn(5L);

        // When
        long count = libraryService.getTotalBooks();

        // Then
        assertThat(count).isEqualTo(5);
    }
}