package com.example.URLShortner.controller;

import com.example.URLShortner.entity.UrlMapping;
import com.example.URLShortner.service.UrlMappingService;
import com.opencsv.exceptions.CsvValidationException;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
public class UrlShortenerController {

    @Autowired
    private UrlMappingService service;

    @PostMapping("/shorten")
    public ResponseEntity<?> shortenUrl(@RequestBody Map<String, String> request) {
        String longUrl = request.get("longUrl");
        UrlMapping url = service.shortenUrl(longUrl);
        return ResponseEntity.ok(Map.of("shortUrl", url.getShortUrl(), "id", url.getId()));
    }

    @PostMapping("/update")
    public boolean updateShortUrl(@RequestParam String shortUrl, @RequestParam String destinationUrl) {
        return service.updateShortUrl(shortUrl, destinationUrl);
    }

    @GetMapping("/{shortUrl}")
    public void redirectToFullUrl(HttpServletResponse response, @PathVariable String shortUrl) throws IOException {
        String destinationUrl = service.getDestinationUrl(shortUrl);
        if (destinationUrl != null) {
            response.sendRedirect(destinationUrl);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Url not found");
        }
    }

    @PostMapping("/updateExpiry")
    public boolean updateExpiry(@RequestParam String shortUrl, @RequestParam int daysToAdd) {
        return service.updateExpiry(shortUrl, daysToAdd);
    }

    @PostMapping("/upload-csv")
    public ResponseEntity<?> uploadCsv(@RequestParam("file") MultipartFile file) {
        try {
            List<Map<String, String>> results = service.processCsv(file);
            return ResponseEntity.ok(results);
        } catch (IOException | CsvValidationException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Failed to process CSV file.");
        }
    }

    @PostMapping("/process-csv-file")
    public ResponseEntity<?> processCsvFile(@RequestParam("filePath") String filePath) {
        try {
            List<Map<String, String>> results = service.processCsvFile(filePath);
            return ResponseEntity.ok(results);
        } catch (IOException | CsvValidationException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Failed to process CSV file.");
        }
    }
}