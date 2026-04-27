package com.example.lotto.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "winning_draws")
public class WinningDrawEntity {

    @Id
    private Integer drawNumber;

    private LocalDate drawDate;

    @Column(nullable = false)
    private int number1;

    @Column(nullable = false)
    private int number2;

    @Column(nullable = false)
    private int number3;

    @Column(nullable = false)
    private int number4;

    @Column(nullable = false)
    private int number5;

    @Column(nullable = false)
    private int number6;

    private Integer bonusNumber;

    protected WinningDrawEntity() {
    }

    public WinningDrawEntity(Integer drawNumber, LocalDate drawDate, List<Integer> numbers, Integer bonusNumber) {
        this.drawNumber = drawNumber;
        this.drawDate = drawDate;
        this.number1 = numbers.get(0);
        this.number2 = numbers.get(1);
        this.number3 = numbers.get(2);
        this.number4 = numbers.get(3);
        this.number5 = numbers.get(4);
        this.number6 = numbers.get(5);
        this.bonusNumber = bonusNumber;
    }

    public Integer getDrawNumber() {
        return drawNumber;
    }

    public LocalDate getDrawDate() {
        return drawDate;
    }

    public List<Integer> getNumbers() {
        return List.of(number1, number2, number3, number4, number5, number6);
    }

    public Integer getBonusNumber() {
        return bonusNumber;
    }
}
