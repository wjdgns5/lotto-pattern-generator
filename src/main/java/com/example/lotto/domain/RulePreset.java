package com.example.lotto.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "rule_presets")
public class RulePreset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80)
    private String name;

    @Column(nullable = false, length = 60)
    private String username;

    private int threeOddThreeEvenCount;
    private int fourOddTwoEvenCount;
    private int minSum;
    private int maxSum;

    @Column(length = 300)
    private String excludedNumbers;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    protected RulePreset() {
    }

    public RulePreset(String name, String username, int threeOddThreeEvenCount, int fourOddTwoEvenCount,
                      int minSum, int maxSum, String excludedNumbers) {
        this.name = name;
        this.username = username;
        this.threeOddThreeEvenCount = threeOddThreeEvenCount;
        this.fourOddTwoEvenCount = fourOddTwoEvenCount;
        this.minSum = minSum;
        this.maxSum = maxSum;
        this.excludedNumbers = excludedNumbers == null ? "" : excludedNumbers;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getUsername() {
        return username;
    }

    public int getThreeOddThreeEvenCount() {
        return threeOddThreeEvenCount;
    }

    public int getFourOddTwoEvenCount() {
        return fourOddTwoEvenCount;
    }

    public int getMinSum() {
        return minSum;
    }

    public int getMaxSum() {
        return maxSum;
    }

    public String getExcludedNumbers() {
        return excludedNumbers;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
