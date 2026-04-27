package com.example.lotto.model;

import java.util.List;

public class LottoCandidate {

    private final List<Integer> numbers;
    private final int sum;
    private final String oddEvenPattern;
    private final List<Integer> lowFrequencyNumbers;

    public LottoCandidate(List<Integer> numbers, int sum, String oddEvenPattern, List<Integer> lowFrequencyNumbers) {
        this.numbers = numbers;
        this.sum = sum;
        this.oddEvenPattern = oddEvenPattern;
        this.lowFrequencyNumbers = lowFrequencyNumbers;
    }

    public List<Integer> getNumbers() {
        return numbers;
    }

    public int getSum() {
        return sum;
    }

    public String getOddEvenPattern() {
        return oddEvenPattern;
    }

    public List<Integer> getLowFrequencyNumbers() {
        return lowFrequencyNumbers;
    }

    public String getCopyText() {
        return numbers.stream()
                .map(String::valueOf)
                .reduce((left, right) -> left + ", " + right)
                .orElse("");
    }
}
