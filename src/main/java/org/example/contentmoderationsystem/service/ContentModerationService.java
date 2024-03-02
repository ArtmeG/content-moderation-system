package org.example.contentmoderationsystem.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Service
@Slf4j
@RequiredArgsConstructor
public class ContentModerationService {

    public static final String CSV_SEPARATOR = ",";
    public static final String HEADER_COLUMN = "user_id,total_messages,avg_score";
    public static final String COMMA_DELIMITER = ",";
    private final TranslationService translationService;
    private final ScoringService scoringService;
    private static final ExecutorService EXECUTOR_SERVICE = Executors.newWorkStealingPool(1000);

    public void processCSV(@NonNull final MultipartFile file, @NonNull final PrintWriter writer) throws IOException {
        Assert.notNull(file, "The file must be initialized.");
        Assert.notNull(writer, "The PrintWriter must be initialized.");

        if (file.isEmpty()) {
            throw new IOException("File is empty");
        }

        Map<String, UserMessageStats> userStatsMap = new HashMap<>();
        ArrayList<Future<?>> task = new ArrayList<>();

        // Read input CSV file
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            int linrCount = 0;

            while ((line = reader.readLine()) != null) {
                int currentLine = linrCount++;
                String notProcessingLine = line;
                task.add(EXECUTOR_SERVICE.submit(() ->
                        taskLineProcessing(currentLine, notProcessingLine, userStatsMap)));
            }
        } catch (IOException e) {
            log.error("Error occurred during reading line from file,", e);
            throw new RuntimeException(e);
        }

        task.parallelStream().forEach(future -> {
            try {
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                log.error("Error occurred while processing task ", e);
                throw new RuntimeException(e);
            }
        });

        // Write output CSV file
        writeResult(writer, userStatsMap);
    }

    private static void writeResult(final PrintWriter writer, final Map<String, UserMessageStats> userStatsMap) {
        writer.println(HEADER_COLUMN);
        for (Map.Entry<String, UserMessageStats> entry : userStatsMap.entrySet()) {
            String userId = entry.getKey();
            UserMessageStats stats = entry.getValue();
            writer.println(userId + COMMA_DELIMITER + stats.totalMessage() + COMMA_DELIMITER + stats.getAverageScore());
        }
        writer.flush();
    }

    private void taskLineProcessing(final int lineNumber, final String line, final Map<String, UserMessageStats> userStatsMap) {
        String[] parts = line.split(CSV_SEPARATOR);
        if (parts.length < 2) {
            log.error("The line {} on row number {} didn't processing, cause wrong format ", line, lineNumber);
            return;
        }
        String userId = parts[0];
        String message = parts[1];

        // Translate message to English
        String translatedMessage = translationService.translateMessage(message);

        // Get offensiveness score
        float score = scoringService.getOffensivenessScore(translatedMessage);

        // Update user stats
        userStatsMap.compute(userId, (k, v) -> {
            if (v == null) {
                return new UserMessageStats(1, score);
            } else {
                return UserMessageStats.newInstance(v, score);
            }
        });
    }

    private record UserMessageStats(int totalMessage, float totalScore) {

        private static UserMessageStats newInstance(UserMessageStats old, float score) {
            return new UserMessageStats(old.totalMessage + 1, old.totalScore + score);
        }

        public float getAverageScore() {
            return totalMessage > 0 ? totalScore / totalMessage : 0;
        }
    }
}
