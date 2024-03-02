package org.example.contentmoderationsystem.service;

import org.springframework.stereotype.Service;

@Service
public class ScoringService {
    public float getOffensivenessScore(String message) {
        // Simulate scoring service
        // Add logic to interact with the actual scoring service
        // Simulate network latency
        try {
            Thread.sleep((long) (50 + Math.random() * 150));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return (float) Math.random(); // Simulated score
    }
}
