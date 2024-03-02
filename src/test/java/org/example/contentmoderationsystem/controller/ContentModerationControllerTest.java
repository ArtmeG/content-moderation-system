package org.example.contentmoderationsystem.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.example.contentmoderationsystem.service.ContentModerationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class ContentModerationControllerTest {

    @Mock
    private ContentModerationService contentModerationService;

    private ContentModerationController contentModerationController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        contentModerationController = new ContentModerationController(contentModerationService);
    }

    @Test
    void processCSV_EmptyFile() throws IOException {
        // Given
        MultipartFile file = new MockMultipartFile("test.csv", new byte[0]);
        MockHttpServletResponse response = new MockHttpServletResponse();

        // When
        contentModerationController.processCSV(file, response);

        // Then
        verify(contentModerationService, never()).processCSV(any(), any());
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatus());
    }

    @Test
    void processCSV_SuccessfulProcessing() throws IOException {
        // Given
        String csvContent = "user1,message1\nuser2,message2";
        MultipartFile file = new MockMultipartFile("test.csv", new ByteArrayInputStream(csvContent.getBytes()));
        MockHttpServletResponse response = new MockHttpServletResponse();

        // When
        contentModerationController.processCSV(file, response);

        // Then
        verify(contentModerationService, times(1)).processCSV(eq(file), any());
        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        assertEquals("text/csv", response.getContentType());
        assertEquals("attachment; filename=processed.csv", response.getHeader("Content-Disposition"));
    }
}
