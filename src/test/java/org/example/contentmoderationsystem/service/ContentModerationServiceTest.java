package org.example.contentmoderationsystem.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

class ContentModerationServiceTest {

    @Mock
    private TranslationService translationService;

    @Mock
    private ScoringService scoringService;

    @Mock
    private MockMultipartFile file;

    private ContentModerationService contentModerationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        contentModerationService = new ContentModerationService(translationService, scoringService);
    }

    @Test
    void testProcessCSV_Success() throws IOException {
        String csvData = "user1,message1\nuser2,message2\nuser1,message3\n";
        MockMultipartFile file = new MockMultipartFile("file", "test.csv"
                , "text/csv", csvData.getBytes());
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);

        // Mock behavior
        when(translationService.translateMessage("message1")).thenReturn("message1");
        when(translationService.translateMessage("message2")).thenReturn("message2");
        when(translationService.translateMessage("message3")).thenReturn("message3");
        when(scoringService.getOffensivenessScore("message1")).thenReturn(0.1f);
        when(scoringService.getOffensivenessScore("message2")).thenReturn(0.1f);
        when(scoringService.getOffensivenessScore("message3")).thenReturn(0.1f);

        // Execute method under test
        contentModerationService.processCSV(file, writer);

        // Verify expected output
        String expectedOutput = "user_id,total_messages,avg_score\n" +
                "user1,2,0.1\n" +
                "user2,1,0.1\n";
        assertEquals(expectedOutput, stringWriter.toString());
    }

    @Test
    void testProcessCSV_EmptyFile() throws IOException {
        MockMultipartFile file = new MockMultipartFile("file", "test.csv"
                , "text/csv", "".getBytes());
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);

        contentModerationService.processCSV(file, writer);

        String expectedOutput = "user_id,total_messages,avg_score\n";
        assertEquals(expectedOutput, stringWriter.toString());
    }

    @Test
    void testProcessCSV_Exception() throws IOException {
        file = new MockMultipartFile("file", "test_empty.csv", "text/csv", "".getBytes());
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);

        assertThrows(IOException.class, () -> contentModerationService.processCSV(file, writer));
    }
}
