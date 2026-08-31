package com.example.librarymanagement.repository;

import com.example.librarymanagement.model.Book;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class BookRepository {
    private final Map<String, Book> books = new ConcurrentHashMap<>();
    private final Map<String, String> isbnToId = new ConcurrentHashMap<>();

    public Book save(Book book) {
        if (book == null) {
            throw new IllegalArgumentException("Book cannot be null");
        }

        if (book.getId() == null || book.getId().isEmpty()) {
            book.setId(UUID.randomUUID().toString());
        }

        String existingId = isbnToId.get(book.getIsbn());
        if (existingId != null && !existingId.equals(book.getId())) {
            throw new IllegalStateException("ISBN already exists for another book");
        }

        books.put(book.getId(), book);
        isbnToId.put(book.getIsbn(), book.getId());

        return book;
    }

    public Optional<Book> findById(String id) {
        if (id == null || id.isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(books.get(id));
    }

    public Optional<Book> findByIsbn(String isbn) {
        if (isbn == null || isbn.isEmpty()) {
            return Optional.empty();
        }
        String id = isbnToId.get(isbn);
        return id != null ? Optional.ofNullable(books.get(id)) : Optional.empty();
    }

    public List<Book> findAll() {
        return new ArrayList<>(books.values());
    }

    public List<Book> findAvailable() {
        return books.values().stream()
                .filter(Book::isAvailable)
                .collect(Collectors.toList());
    }

    public List<Book> findByAuthor(String author) {
        if (author == null || author.isEmpty()) {
            return new ArrayList<>();
        }
        return books.values().stream()
                .filter(book -> book.getAuthor().equalsIgnoreCase(author))
                .collect(Collectors.toList());
    }

    public List<Book> findByTitle(String title) {
        if (title == null || title.isEmpty()) {
            return new ArrayList<>();
        }
        return books.values().stream()
                .filter(book -> book.getTitle().toLowerCase().contains(title.toLowerCase()))
                .collect(Collectors.toList());
    }

    public boolean delete(String id) {
        if (id == null || id.isEmpty()) {
            return false;
        }
        Book removed = books.remove(id);
        if (removed != null) {
            isbnToId.remove(removed.getIsbn());
            return true;
        }
        return false;
    }

    public long count() {
        return books.size();
    }

    public long countAvailable() {
        return books.values().stream().filter(Book::isAvailable).count();
    }

    public boolean exists(String id) {
        return id != null && books.containsKey(id);
    }

    public boolean existsByIsbn(String isbn) {
        return isbn != null && isbnToId.containsKey(isbn);
    }

    public void clear() {
        books.clear();
        isbnToId.clear();
    }

    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalBooks", books.size());
        stats.put("availableBooks", countAvailable());
        stats.put("borrowedBooks", books.size() - countAvailable());
        return stats;
    }
}