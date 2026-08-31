package com.example.librarymanagement.integration;

import com.example.librarymanagement.model.Book;
import com.example.librarymanagement.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class RepositoryIntegrationTest {

    private BookRepository repository;

    @BeforeEach
    void setUp() {
        repository = new BookRepository();
    }

    @Test
    void shouldHandleMultipleOperationsSequentially() {
        // Given
        Book book1 = new Book(null, "Book1", "Author1", "ISBN1", 2000);
        Book book2 = new Book(null, "Book2", "Author2", "ISBN2", 2001);
        Book book3 = new Book(null, "Book3", "Author3", "ISBN3", 2002);

        // When - Save
        repository.save(book1);
        repository.save(book2);
        repository.save(book3);

        // Then - Count
        assertThat(repository.count()).isEqualTo(3);

        // When - Update
        Book bookToUpdate = repository.findById(book1.getId()).orElseThrow();
        bookToUpdate.setTitle("Updated Title");
        repository.save(bookToUpdate);

        // Then - Update
        Book updated = repository.findById(book1.getId()).orElseThrow();
        assertThat(updated.getTitle()).isEqualTo("Updated Title");

        // When - Delete
        repository.delete(book2.getId());

        // Then - Delete
        assertThat(repository.count()).isEqualTo(2);
        assertThat(repository.findById(book2.getId())).isEmpty();

        // When - Clear
        repository.clear();

        // Then - Clear
        assertThat(repository.count()).isZero();
    }

    @Test
    void shouldHandleConcurrentOperations() throws InterruptedException {
        // Given
        int threadCount = 10;
        Thread[] threads = new Thread[threadCount];

        // When
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            threads[i] = new Thread(() -> {
                Book book = new Book(null, "Book" + index, "Author" + index,
                        "ISBN" + UUID.randomUUID().toString(), 2000 + index);
                repository.save(book);
            });
        }

        for (Thread t : threads) {
            t.start();
        }

        for (Thread t : threads) {
            t.join();
        }

        // Then
        assertThat(repository.count()).isEqualTo(threadCount);
    }

    @Test
    void shouldEnforceIsbnUniqueness() {
        // Given
        Book book1 = new Book(null, "Book1", "Author1", "SAME-ISBN", 2000);
        Book book2 = new Book(null, "Book2", "Author2", "SAME-ISBN", 2001);

        // When
        repository.save(book1);

        // Then
        assertThatThrownBy(() -> repository.save(book2))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already exists");
    }
}