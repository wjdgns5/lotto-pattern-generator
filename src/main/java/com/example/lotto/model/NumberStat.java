package com.example.lotto.model;

public class NumberStat {

    private final int number;
    private final long count;

    public NumberStat(int number, long count) {
        this.number = number;
        this.count = count;
    }

    public int getNumber() {
        return number;
    }

    public long getCount() {
        return count;
    }
}
