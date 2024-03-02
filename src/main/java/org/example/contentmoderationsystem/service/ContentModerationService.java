package org.example.contentmoderationsystem.service;

import lombok.Getter;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

@Service
public class ContentModerationService {

    private final TranslationService translationService;
    private final ScoringService scoringService;

    public ContentModerationService(TranslationService translationService, ScoringService scoringService) {
        this.translationService = translationService;
        this.scoringService = scoringService;
    }


    public void processCSV(MultipartFile file, PrintWriter writer) throws IOException {
        Map<String, UserMessageStats> userStatsMap = new HashMap<>();

        if (file.isEmpty()) {
            throw new IOException("File is empty");
        }
        // Read input CSV file
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                String userId = parts[0];
                String message = parts[1];

                // Translate message to English
                String translatedMessage = translationService.translateMessage(message);

                // Get offensiveness score
                float score = scoringService.getOffensivenessScore(translatedMessage);

                // Update user stats
                userStatsMap.computeIfAbsent(userId, k -> new UserMessageStats()).addMessage(score);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Write output CSV file
        writer.println("user_id,total_messages,avg_score");
        for (Map.Entry<String, UserMessageStats> entry : userStatsMap.entrySet()) {
            String userId = entry.getKey();
            UserMessageStats stats = entry.getValue();
            writer.println(userId + "," + stats.getTotalMessages() + "," + stats.getAverageScore());
        }
        writer.flush();
    }

    static class UserMessageStats {
        @Getter
        private int totalMessages;
        private float totalScore;

        public void addMessage(float score) {
            totalMessages++;
            totalScore += score;
        }

        public float getAverageScore() {
            return totalMessages > 0 ? totalScore / totalMessages : 0;
        }
    }
}
