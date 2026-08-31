package com.example.librarymanagement.repository;

import com.example.librarymanagement.model.Member;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class MemberRepository {
    private final Map<String, Member> members = new ConcurrentHashMap<>();
    private final Map<String, String> emailToId = new ConcurrentHashMap<>();

    public Member save(Member member) {
        if (member == null) {
            throw new IllegalArgumentException("Member cannot be null");
        }

        if (member.getId() == null || member.getId().isEmpty()) {
            member.setId(UUID.randomUUID().toString());
        }

        String existingId = emailToId.get(member.getEmail());
        if (existingId != null && !existingId.equals(member.getId())) {
            throw new IllegalStateException("Email already exists for another member");
        }

        members.put(member.getId(), member);
        emailToId.put(member.getEmail(), member.getId());

        return member;
    }

    public Optional<Member> findById(String id) {
        if (id == null || id.isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(members.get(id));
    }

    public Optional<Member> findByEmail(String email) {
        if (email == null || email.isEmpty()) {
            return Optional.empty();
        }
        String id = emailToId.get(email);
        return id != null ? Optional.ofNullable(members.get(id)) : Optional.empty();
    }

    public List<Member> findAll() {
        return new ArrayList<>(members.values());
    }

    public List<Member> findActiveMembers() {
        return members.values().stream()
                .filter(Member::isActive)
                .collect(Collectors.toList());
    }

    public List<Member> findMembersWithBorrowedBooks() {
        return members.values().stream()
                .filter(member -> !member.getBorrowedBookIds().isEmpty())
                .collect(Collectors.toList());
    }

    public boolean delete(String id) {
        if (id == null || id.isEmpty()) {
            return false;
        }
        Member removed = members.remove(id);
        if (removed != null) {
            emailToId.remove(removed.getEmail());
            return true;
        }
        return false;
    }

    public long count() {
        return members.size();
    }

    public boolean exists(String id) {
        return id != null && members.containsKey(id);
    }

    public void clear() {
        members.clear();
        emailToId.clear();
    }
}