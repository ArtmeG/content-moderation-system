package org.example.contentmoderationsystem.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.contentmoderationsystem.service.ContentModerationService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.PrintWriter;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ContentModerationController {

    private final ContentModerationService contentModerationService;

    @PostMapping(value = "/process-csv", produces = MediaType.TEXT_PLAIN_VALUE)
    public void processCSV(@RequestParam("file") MultipartFile file, HttpServletResponse response) throws IOException {
        if (file.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=processed.csv");

        contentModerationService.processCSV(file, new PrintWriter(response.getOutputStream()));
    }
}
