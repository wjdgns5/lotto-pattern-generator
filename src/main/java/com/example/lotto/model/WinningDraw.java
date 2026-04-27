package com.example.lotto.model;

import java.time.LocalDate;
import java.util.List;

public class WinningDraw {

    private final int drawNumber;
    private final LocalDate drawDate;
    private final List<Integer> numbers;
    private final Integer bonusNumber;

    public WinningDraw(int drawNumber, LocalDate drawDate, List<Integer> numbers, Integer bonusNumber) {
        this.drawNumber = drawNumber;
        this.drawDate = drawDate;
        this.numbers = numbers;
        this.bonusNumber = bonusNumber;
    }

    public int getDrawNumber() {
        return drawNumber;
    }

    public LocalDate getDrawDate() {
        return drawDate;
    }

    public List<Integer> getNumbers() {
        return numbers;
    }

    public Integer getBonusNumber() {
        return bonusNumber;
    }

    public String toCsvLine() {
        return drawNumber + ","
                + (drawDate == null ? "" : drawDate) + ","
                + numbers.get(0) + ","
                + numbers.get(1) + ","
                + numbers.get(2) + ","
                + numbers.get(3) + ","
                + numbers.get(4) + ","
                + numbers.get(5) + ","
                + (bonusNumber == null ? "" : bonusNumber);
    }
}
