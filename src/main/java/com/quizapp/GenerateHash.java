package com.quizapp;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class GenerateHash {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
        String raw = "Admin@456";
        String encoded = encoder.encode(raw);
        System.out.println(encoded);
    }
}
