package com.example.lotto.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "generation_histories")
public class GenerationHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 60)
    private String username;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false, length = 300)
    private String requestSummary;

    @Lob
    @Column(nullable = false)
    private String resultText;

    protected GenerationHistory() {
    }

    public GenerationHistory(String username, String requestSummary, String resultText) {
        this.username = username;
        this.requestSummary = requestSummary;
        this.resultText = resultText;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getRequestSummary() {
        return requestSummary;
    }

    public String getResultText() {
        return resultText;
    }
}
