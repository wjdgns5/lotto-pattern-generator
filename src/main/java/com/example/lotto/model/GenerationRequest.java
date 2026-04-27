package com.example.lotto.model;

public class GenerationRequest {

    private int threeOddThreeEvenCount = 5;
    private int fourOddTwoEvenCount = 5;
    private int minSum = 120;
    private int maxSum = 150;
    private String excludedNumbers = "";

    public int getThreeOddThreeEvenCount() {
        return threeOddThreeEvenCount;
    }

    public void setThreeOddThreeEvenCount(int threeOddThreeEvenCount) {
        this.threeOddThreeEvenCount = threeOddThreeEvenCount;
    }

    public int getFourOddTwoEvenCount() {
        return fourOddTwoEvenCount;
    }

    public void setFourOddTwoEvenCount(int fourOddTwoEvenCount) {
        this.fourOddTwoEvenCount = fourOddTwoEvenCount;
    }

    public int getMinSum() {
        return minSum;
    }

    public void setMinSum(int minSum) {
        this.minSum = minSum;
    }

    public int getMaxSum() {
        return maxSum;
    }

    public void setMaxSum(int maxSum) {
        this.maxSum = maxSum;
    }

    public String getExcludedNumbers() {
        return excludedNumbers;
    }

    public void setExcludedNumbers(String excludedNumbers) {
        this.excludedNumbers = excludedNumbers;
    }
}
