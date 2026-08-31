package com.example.librarymanagement;

import com.example.librarymanagement.model.Book;
import com.example.librarymanagement.service.LibraryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

@Slf4j
@SpringBootApplication
public class LibraryManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(LibraryManagementApplication.class, args);
        log.info("Library Management System started successfully!");
    }

    @Bean
    @Profile("!test")  // Ne pas exécuter en mode test
    public CommandLineRunner initData(LibraryService libraryService) {
        return args -> {
            log.info("Initializing sample data...");
            try {
                // Vérifier si la base est vide
                if (libraryService.getTotalBooks() == 0) {
                    Book book1 = new Book(null, "Clean Code", "Robert Martin", "978-0132350884", 2008);
                    Book book2 = new Book(null, "The Pragmatic Programmer", "Andrew Hunt", "978-0201616224", 1999);
                    Book book3 = new Book(null, "Design Patterns", "Erich Gamma", "978-0201633610", 1994);
                    Book book4 = new Book(null, "Code Complete", "Steve McConnell", "978-0735619678", 2004);
                    Book book5 = new Book(null, "Refactoring", "Martin Fowler", "978-0201485677", 1999);

                    libraryService.addBook(book1);
                    libraryService.addBook(book2);
                    libraryService.addBook(book3);
                    libraryService.addBook(book4);
                    libraryService.addBook(book5);

                    log.info("Sample books added: {} books available", libraryService.getTotalBooks());
                } else {
                    log.info("Database already contains {} books", libraryService.getTotalBooks());
                }
            } catch (Exception e) {
                log.error("Error initializing sample data", e);
            }
        };
    }
}