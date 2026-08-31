package com.example.librarymanagement.service;

import com.example.librarymanagement.model.Book;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class NotificationService {

    public void notifyNewBookAdded(Book book) {
        log.info("New book added: '{}' by {}", book.getTitle(), book.getAuthor());
        // Implémentation réelle des notifications
        // sendEmail("admin@library.com", "New Book Added", "Book: " + book.getTitle());
        // sendSlackNotification("New book: " + book.getTitle());
    }

    public void notifyBookBorrowed(Book book) {
        log.info("Book borrowed: '{}'", book.getTitle());
        // sendEmail("member@example.com", "Book Borrowed", "You borrowed: " + book.getTitle());
    }

    public void notifyBookReturned(Book book) {
        log.info("Book returned: '{}'", book.getTitle());
        // sendEmail("member@example.com", "Book Returned", "You returned: " + book.getTitle());
    }

    public void sendOverdueNotification(String memberId, List<Book> overdueBooks) {
        log.warn("Member {} has {} overdue books", memberId, overdueBooks.size());
        // Implémentation réelle
    }

    public void notifyBookDeleted(Book book) {
        log.info("Book deleted: '{}'", book.getTitle());
        // sendEmail("admin@library.com", "Book Deleted", "Book: " + book.getTitle());
    }

    public void sendDailyReport(int totalBooks, int borrowedBooks, int availableBooks) {
        log.info("Daily Report: Total={}, Borrowed={}, Available={}",
                totalBooks, borrowedBooks, availableBooks);
        // sendEmail("admin@library.com", "Daily Report",
        //    String.format("Total: %d, Borrowed: %d, Available: %d", totalBooks, borrowedBooks, availableBooks));
    }

    private void sendEmail(String to, String subject, String body) {
        // Implémentation d'envoi d'email
        log.info("Email sent to {}: {} - {}", to, subject, body.substring(0, Math.min(50, body.length())));
    }

    private void sendSlackNotification(String message) {
        // Implémentation Slack
        log.info("Slack notification: {}", message);
    }
}