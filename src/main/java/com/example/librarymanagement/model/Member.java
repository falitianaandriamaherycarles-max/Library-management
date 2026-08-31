package com.example.librarymanagement.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Member {
    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private LocalDate membershipDate;
    private boolean active;
    private List<String> borrowedBookIds;
    private int maxBooksAllowed;

    public Member(String id, String firstName, String lastName, String email) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.borrowedBookIds = new ArrayList<>();
        this.active = true;
        this.membershipDate = LocalDate.now();
        this.maxBooksAllowed = 5;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public boolean canBorrowMore() {
        return borrowedBookIds.size() < maxBooksAllowed;
    }

    public void borrowBook(String bookId) {
        if (!canBorrowMore()) {
            throw new IllegalStateException("Member has reached maximum borrow limit");
        }
        borrowedBookIds.add(bookId);
    }

    public void returnBook(String bookId) {
        borrowedBookIds.remove(bookId);
    }

    public boolean hasBorrowedBook(String bookId) {
        return borrowedBookIds.contains(bookId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Member member = (Member) o;
        return Objects.equals(id, member.id) || Objects.equals(email, member.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, email);
    }

    @Override
    public String toString() {
        return String.format("Member{id='%s', name='%s %s', email='%s', active=%s, borrowed=%d}",
                id, firstName, lastName, email, active, borrowedBookIds.size());
    }
}