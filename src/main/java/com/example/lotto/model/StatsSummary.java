package com.example.lotto.model;

import java.util.List;
import java.util.Map;

public class StatsSummary {

    private final long drawCount;
    private final List<NumberStat> allNumbers;
    private final List<NumberStat> topNumbers;
    private final List<NumberStat> bottomNumbers;
    private final Map<String, Long> oddEvenRatios;
    private final Map<String, Long> sumRanges;
    private final long maxNumberCount;

    public StatsSummary(
            long drawCount,
            List<NumberStat> allNumbers,
            List<NumberStat> topNumbers,
            List<NumberStat> bottomNumbers,
            Map<String, Long> oddEvenRatios,
            Map<String, Long> sumRanges,
            long maxNumberCount
    ) {
        this.drawCount = drawCount;
        this.allNumbers = allNumbers;
        this.topNumbers = topNumbers;
        this.bottomNumbers = bottomNumbers;
        this.oddEvenRatios = oddEvenRatios;
        this.sumRanges = sumRanges;
        this.maxNumberCount = maxNumberCount;
    }

    public long getDrawCount() {
        return drawCount;
    }

    public List<NumberStat> getAllNumbers() {
        return allNumbers;
    }

    public List<NumberStat> getTopNumbers() {
        return topNumbers;
    }

    public List<NumberStat> getBottomNumbers() {
        return bottomNumbers;
    }

    public Map<String, Long> getOddEvenRatios() {
        return oddEvenRatios;
    }

    public Map<String, Long> getSumRanges() {
        return sumRanges;
    }

    public long getMaxNumberCount() {
        return maxNumberCount;
    }
}
