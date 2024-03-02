package org.example.contentmoderationsystem.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.mockito.Mockito.*;

class ContentModerationServiceTest {

    @Mock
    private TranslationService translationService;

    @Mock
    private ScoringService scoringService;

    private ContentModerationService contentModerationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        contentModerationService = new ContentModerationService(translationService, scoringService);
    }

    @Test
    void processCSV_SuccessfulProcessing() throws IOException {
        // Given
        String csvContent = "user1,message1\nuser2,message2";
        MultipartFile file = new MockMultipartFile("test.csv", new ByteArrayInputStream(csvContent.getBytes()));
        StringWriter writer = new StringWriter();

        when(translationService.translateMessage(anyString())).thenReturn("translated");
        when(scoringService.getOffensivenessScore(anyString())).thenReturn(0.5f);

        // When
        contentModerationService.processCSV(file, new PrintWriter(writer));

        // Then
        verify(translationService, times(2)).translateMessage(anyString());
        verify(scoringService, times(2)).getOffensivenessScore(anyString());
        Assert.notNull(writer.toString(), "Processed content should not be null");
    }

    @Test
    void processCSV_EmptyFile() {
        // Given
        MultipartFile file = new MockMultipartFile("empty.csv", new byte[0]);
        StringWriter writer = new StringWriter();

        // When/Then
        Assert.isTrue(file.isEmpty(), "File should be empty");
        IOException exception = org.junit.jupiter.api.Assertions.assertThrows(IOException.class, () ->
                contentModerationService.processCSV(file, new PrintWriter(writer))
        );
        org.junit.jupiter.api.Assertions.assertEquals("File is empty", exception.getMessage());
    }

    @Test
    void processCSV_ExceptionDuringProcessing() throws IOException {
        // Given
        String csvContent = "user1,message1";
        MultipartFile file = new MockMultipartFile("test.csv", new ByteArrayInputStream(csvContent.getBytes()));
        StringWriter writer = new StringWriter();

        when(translationService.translateMessage(anyString())).thenReturn("translated");
        when(scoringService.getOffensivenessScore(anyString())).thenThrow(Error.class);

        // When/Then
        org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class, () ->
                contentModerationService.processCSV(file, new PrintWriter(writer))
        );
    }
}
