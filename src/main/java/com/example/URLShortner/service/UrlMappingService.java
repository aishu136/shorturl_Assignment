package com.example.URLShortner.service;

import com.example.URLShortner.entity.UrlMapping;
import com.example.URLShortner.repository.UrlMappingRepository;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class UrlMappingService {

    @Autowired
    private UrlMappingRepository urlRepository;



        public UrlMapping shortenUrl(String longUrl) {
            String shortUrl = generateShortUrl(longUrl);
            LocalDateTime expiryDate = LocalDateTime.now().plusMonths(10);
            UrlMapping url = new UrlMapping(UUID.randomUUID(), shortUrl, longUrl, expiryDate, LocalDateTime.now());
            return urlRepository.save(url);
        }

        private String generateShortUrl(String longUrl) {
            String hash = DigestUtils.sha256Hex(longUrl + UUID.randomUUID().toString());
            return  "http://localhost:8080/" + base62Encode(hash).substring(0, 30);
        }

        private String base62Encode(String input) {
            String base62 = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
            StringBuilder encoded = new StringBuilder();
            BigInteger number = new BigInteger(input, 16);

            while (number.compareTo(BigInteger.ZERO) > 0) {
                BigInteger[] divmod = number.divideAndRemainder(BigInteger.valueOf(62));
                encoded.append(base62.charAt(divmod[1].intValue()));
                number = divmod[0];
            }

            return encoded.reverse().toString();
        }

        public List<Map<String, String>> processCsvFile(String filePath) throws IOException, CsvValidationException {
            List<Map<String, String>> results = new ArrayList<>();
            try (CSVReader csvReader = new CSVReader(new FileReader(filePath))) {
                String[] values;
                while ((values = csvReader.readNext()) != null) {
                    if (values.length > 0) {
                        String longUrl = values[0];
                        UrlMapping url = shortenUrl(longUrl);
                        Map<String, String> result = new HashMap<>();
                        result.put("longUrl", longUrl);
                        result.put("shortUrl", url.getShortUrl());
                        result.put("id", url.getId().toString());
                        results.add(result);
                    }
                }
            }
            return results;
        }

        public List<Map<String, String>> processCsv(MultipartFile file) throws IOException, CsvValidationException {
            List<Map<String, String>> results = new ArrayList<>();
            try (CSVReader csvReader = new CSVReader(new InputStreamReader(file.getInputStream()))) {
                String[] values;
                while ((values = csvReader.readNext()) != null) {
                    if (values.length > 0) {
                        String longUrl = values[0];
                        UrlMapping url = shortenUrl(longUrl);
                        Map<String, String> result = new HashMap<>();
                        result.put("longUrl", longUrl);
                        result.put("shortUrl", url.getShortUrl());
                        result.put("id", url.getId().toString());
                        results.add(result);
                    }
                }
            }
            return results;
        }
        public boolean updateShortUrl(String shortUrl, String newDestinationUrl) {
            Optional<UrlMapping> optionalUrlMapping = urlRepository.findByShortUrl(shortUrl);
            if (optionalUrlMapping.isPresent()) {
                UrlMapping existingUrlMapping = optionalUrlMapping.get();
                existingUrlMapping.setDestinationUrl(newDestinationUrl);
                urlRepository.save(existingUrlMapping);
                return true;
            }
            return false;
        }

        public String getDestinationUrl(String shortUrl) {
            Optional<UrlMapping> optionalUrlMapping = urlRepository.findByShortUrl(shortUrl);
            if (optionalUrlMapping.isPresent()) {
                UrlMapping urlMapping = optionalUrlMapping.get();
                if (urlMapping.getExpiryDate().isAfter(LocalDateTime.now())) {
                    return urlMapping.getDestinationUrl();
                }
            }
            throw new NoSuchElementException("Url not found or expired");
        }

        public boolean updateExpiry(String shortUrl, int daysToAdd) {
            Optional<UrlMapping> optionalUrlMapping = urlRepository.findByShortUrl(shortUrl);
            if (optionalUrlMapping.isPresent()) {
                UrlMapping existingUrlMapping = optionalUrlMapping.get();
                existingUrlMapping.setExpiryDate(existingUrlMapping.getExpiryDate().plusDays(daysToAdd));
                urlRepository.save(existingUrlMapping);
                return true;
            }
            return false;
        }
    }
