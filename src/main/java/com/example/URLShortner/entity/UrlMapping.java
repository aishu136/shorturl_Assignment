package com.example.URLShortner.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import java.util.UUID;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UrlMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    @Column(nullable = false)
    private String shortUrl;
    @Column(nullable = false)
    private String destinationUrl;
    @Column(nullable = false)
    private LocalDateTime expiryDate;


    @Column(nullable = false)
    private LocalDateTime createdAt;




}