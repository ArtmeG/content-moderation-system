package org.example.contentmoderationsystem.controller;

import org.example.contentmoderationsystem.service.ContentModerationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.io.PrintWriter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@WebMvcTest(ContentModerationController.class)
@AutoConfigureMockMvc
class ContentModerationControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ContentModerationService contentModerationService;

    @Test
    void testProcessCSVEndpoint() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.csv", MediaType.TEXT_PLAIN_VALUE
                , "user_id,message\n1,Hello world".getBytes());

        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/process-csv")
                        .file(file))
                .andExpect(MockMvcResultMatchers.status().isOk());

        verify(contentModerationService).processCSV(any(), any(PrintWriter.class));
    }

    @Test
    void testProcessCSVEndpoint_EmptyFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test_empty.csv"
                , MediaType.TEXT_PLAIN_VALUE, new byte[0]);

        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/process-csv")
                        .file(file))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }
}