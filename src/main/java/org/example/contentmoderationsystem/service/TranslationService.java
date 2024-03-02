package org.example.contentmoderationsystem.service;

import org.springframework.stereotype.Service;

@Service
public class TranslationService {
    public String translateMessage(String message) {
        // Simulate translation service
        // Add logic to interact with the actual translation service
        // Simulate network latency
        try {
            Thread.sleep((long) (50 + Math.random() * 150));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return message; // Simulated translation
    }
}
