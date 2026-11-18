package com.example.librarymanagement.unit.repository;

import com.example.librarymanagement.model.Book;
import com.example.librarymanagement.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class BookRepositoryTest {

    private BookRepository repository;
    private Book testBook;

    @BeforeEach
    void setUp() {
        repository = new BookRepository();
        testBook = new Book(null, "Test Book", "Test Author", "978-0132350884", 2022);
    }

    @Test
    void shouldSaveAndFindBook() {
        // When
        Book savedBook = repository.save(testBook);

        // Then
        assertThat(savedBook.getId()).isNotNull();
        Optional<Book> foundBook = repository.findById(savedBook.getId());
        assertThat(foundBook).isPresent();
        assertThat(foundBook.get().getTitle()).isEqualTo("Test Book");
    }

    @Test
    void shouldGenerateIdWhenNotProvided() {
        // When
        Book savedBook = repository.save(testBook);

        // Then
        assertThat(savedBook.getId()).isNotNull();
        assertThat(savedBook.getId()).isNotEmpty();
    }

    @Test
    void shouldFindByIsbn() {
        // Given
        repository.save(testBook);

        // When
        Optional<Book> foundBook = repository.findByIsbn("978-0132350884");

        // Then
        assertThat(foundBook).isPresent();
        assertThat(foundBook.get().getIsbn()).isEqualTo("978-0132350884");
    }

    @Test
    void shouldReturnEmptyWhenFindingByUnknownIsbn() {
        // When
        Optional<Book> foundBook = repository.findByIsbn("unknown-isbn");

        // Then
        assertThat(foundBook).isEmpty();
    }

    @Test
    void shouldFindAllBooks() {
        // Given
        repository.save(testBook);
        Book anotherBook = new Book(null, "Another Book", "Another Author", "978-0735619678", 2023);
        repository.save(anotherBook);

        // When
        List<Book> allBooks = repository.findAll();

        // Then
        assertThat(allBooks).hasSize(2);
    }

    @Test
    void shouldFindAvailableBooks() {
        // Given
        Book book1 = new Book(null, "Book1", "Author1", "ISBN1", 2000);
        book1.setAvailable(true);
        Book book2 = new Book(null, "Book2", "Author2", "ISBN2", 2001);
        book2.setAvailable(false);
        Book book3 = new Book(null, "Book3", "Author3", "ISBN3", 2002);
        book3.setAvailable(true);

        repository.save(book1);
        repository.save(book2);
        repository.save(book3);

        // When
        List<Book> availableBooks = repository.findAvailable();

        // Then
        assertThat(availableBooks).hasSize(2);
        assertThat(availableBooks).allMatch(Book::isAvailable);
    }

    @Test
    void shouldDeleteBook() {
        // Given
        Book savedBook = repository.save(testBook);

        // When
        boolean deleted = repository.delete(savedBook.getId());

        // Then
        assertThat(deleted).isTrue();
        assertThat(repository.findById(savedBook.getId())).isEmpty();
        assertThat(repository.findByIsbn(testBook.getIsbn())).isEmpty();
    }

    @Test
    void shouldReturnFalseWhenDeletingUnknownBook() {
        // When
        boolean deleted = repository.delete("unknown-id");

        // Then
        assertThat(deleted).isFalse();
    }

    @Test
    void shouldCountBooks() {
        // Given
        repository.save(testBook);
        repository.save(new Book(null, "Book2", "Author2", "ISBN2", 2023));

        // When
        long count = repository.count();

        // Then
        assertThat(count).isEqualTo(2);
    }

    @Test
    void shouldClearAllBooks() {
        // Given
        repository.save(testBook);
        repository.save(new Book(null, "Book2", "Author2", "ISBN2", 2023));

        // When
        repository.clear();

        // Then
        assertThat(repository.count()).isZero();
    }

    @Test
    void shouldFindByAuthor() {
        // Given
        Book book1 = new Book(null, "Clean Code", "Robert Martin", "ISBN1", 2008);
        Book book2 = new Book(null, "Clean Architecture", "Robert Martin", "ISBN2", 2017);
        Book book3 = new Book(null, "Code Complete", "Steve McConnell", "ISBN3", 2004);

        repository.save(book1);
        repository.save(book2);
        repository.save(book3);

        // When
        List<Book> martinBooks = repository.findByAuthor("Robert Martin");

        // Then
        assertThat(martinBooks).hasSize(2);
        assertThat(martinBooks).allMatch(book -> book.getAuthor().equals("Robert Martin"));
    }

    @Test
    void shouldFindByTitle() {
        // Given
        Book book1 = new Book(null, "Clean Code", "Robert Martin", "ISBN1", 2008);
        Book book2 = new Book(null, "Code Complete", "Steve McConnell", "ISBN2", 2004);

        repository.save(book1);
        repository.save(book2);

        // When
        List<Book> results = repository.findByTitle("Code");

        // Then
        assertThat(results).hasSize(2);
    }

    @Test
    void shouldGetStats() {
        // Given
        Book book1 = new Book(null, "Book1", "Author1", "ISBN1", 2000);
        book1.setAvailable(true);
        Book book2 = new Book(null, "Book2", "Author2", "ISBN2", 2001);
        book2.setAvailable(false);

        repository.save(book1);
        repository.save(book2);

        // When
        var stats = repository.getStats();

        // Then
        assertThat(stats).containsEntry("totalBooks", 2);
        assertThat(stats).containsEntry("availableBooks", 1L);
        assertThat(stats).containsEntry("borrowedBooks", 1L);
    }
}