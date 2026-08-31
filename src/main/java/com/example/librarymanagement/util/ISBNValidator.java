package com.example.librarymanagement.util;

import java.util.regex.Pattern;

public class ISBNValidator {
    private static final Pattern ISBN10_PATTERN = Pattern.compile("^[0-9]{9}[0-9Xx]$");
    private static final Pattern ISBN13_PATTERN = Pattern.compile("^[0-9]{13}$");

    private ISBNValidator() {
        // Classe utilitaire - constructeur privé
    }

    public static boolean isValidISBN(String isbn) {
        if (isbn == null || isbn.isEmpty()) {
            return false;
        }

        // Supprimer les tirets et espaces
        String cleanIsbn = isbn.replaceAll("[- ]", "");

        return isValidISBN10(cleanIsbn) || isValidISBN13(cleanIsbn);
    }

    public static boolean isValidISBN10(String isbn) {
        if (isbn == null || isbn.isEmpty()) {
            return false;
        }

        if (!ISBN10_PATTERN.matcher(isbn).matches()) {
            return false;
        }

        int sum = 0;
        for (int i = 0; i < 10; i++) {
            char c = isbn.charAt(i);
            int digit = (c == 'X' || c == 'x') ? 10 : Character.getNumericValue(c);
            sum += digit * (10 - i);
        }
        return sum % 11 == 0;
    }

    public static boolean isValidISBN13(String isbn) {
        if (isbn == null || isbn.isEmpty()) {
            return false;
        }

        if (!ISBN13_PATTERN.matcher(isbn).matches()) {
            return false;
        }

        int sum = 0;
        for (int i = 0; i < 13; i++) {
            int digit = Character.getNumericValue(isbn.charAt(i));
            sum += (i % 2 == 0) ? digit : digit * 3;
        }
        return sum % 10 == 0;
    }

    public static String normalizeISBN(String isbn) {
        if (isbn == null) {
            return null;
        }
        return isbn.replaceAll("[- ]", "");
    }
}