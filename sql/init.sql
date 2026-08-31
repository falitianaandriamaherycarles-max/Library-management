-- init.sql - Script d'initialisation de la base de données

-- Créer la base de données si elle n'existe pas
CREATE DATABASE IF NOT EXISTS library_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE library_db;

-- Créer la table des livres
CREATE TABLE IF NOT EXISTS books (
                                     id VARCHAR(36) PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    author VARCHAR(100) NOT NULL,
    isbn VARCHAR(20) UNIQUE NOT NULL,
    available BOOLEAN DEFAULT TRUE,
    publication_year INT NOT NULL,
    added_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    description TEXT,
    INDEX idx_isbn (isbn),
    INDEX idx_title (title),
    INDEX idx_author (author)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Créer la table des membres
CREATE TABLE IF NOT EXISTS members (
                                       id VARCHAR(36) PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    phone VARCHAR(20),
    membership_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    active BOOLEAN DEFAULT TRUE,
    max_books_allowed INT DEFAULT 5,
    INDEX idx_email (email),
    INDEX idx_name (first_name, last_name)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Créer la table des emprunts
CREATE TABLE IF NOT EXISTS borrowings (
                                          id VARCHAR(36) PRIMARY KEY,
    book_id VARCHAR(36) NOT NULL,
    member_id VARCHAR(36) NOT NULL,
    borrow_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    due_date DATETIME NOT NULL,
    return_date DATETIME,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE,
    FOREIGN KEY (member_id) REFERENCES members(id) ON DELETE CASCADE,
    INDEX idx_book (book_id),
    INDEX idx_member (member_id),
    INDEX idx_status (status)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Créer un utilisateur pour l'application
CREATE USER IF NOT EXISTS 'library_user'@'%' IDENTIFIED BY 'library_password';
GRANT ALL PRIVILEGES ON library_db.* TO 'library_user'@'%';
FLUSH PRIVILEGES;

-- Insertion de données de test (optionnel)
INSERT INTO books (id, title, author, isbn, publication_year, description) VALUES
                                                                               (UUID(), 'Clean Code', 'Robert Martin', '978-0132350884', 2008, 'A handbook of agile software craftsmanship'),
                                                                               (UUID(), 'The Pragmatic Programmer', 'Andrew Hunt', '978-0201616224', 1999, 'From journeyman to master'),
                                                                               (UUID(), 'Design Patterns', 'Erich Gamma', '978-0201633610', 1994, 'Elements of Reusable Object-Oriented Software'),
                                                                               (UUID(), 'Code Complete', 'Steve McConnell', '978-0735619678', 2004, 'A practical handbook of software construction'),
                                                                               (UUID(), 'Refactoring', 'Martin Fowler', '978-0201485677', 1999, 'Improving the design of existing code')
    ON DUPLICATE KEY UPDATE title = VALUES(title);

-- Insertion de membres de test
INSERT INTO members (id, first_name, last_name, email, phone) VALUES
                                                                  (UUID(), 'John', 'Doe', 'john.doe@example.com', '+1234567890'),
                                                                  (UUID(), 'Jane', 'Smith', 'jane.smith@example.com', '+0987654321'),
                                                                  (UUID(), 'Bob', 'Johnson', 'bob.johnson@example.com', '+1122334455')
    ON DUPLICATE KEY UPDATE email = VALUES(email);