package com.example.lotto.service;

import com.example.lotto.model.NumberStat;
import com.example.lotto.model.StatsSummary;
import com.example.lotto.model.WinningDraw;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

@Service
public class StatsService {

    private final WinningNumberService winningNumberService;

    public StatsService(WinningNumberService winningNumberService) {
        this.winningNumberService = winningNumberService;
    }

    public StatsSummary summarize() {
        // 통계 화면에 필요한 모든 데이터를 한 번에 계산해 StatsSummary로 묶어 반환합니다.
        List<WinningDraw> draws = winningNumberService.findAll();
        Map<Integer, Long> numberCounts = new LinkedHashMap<>();
        IntStream.rangeClosed(1, 45).forEach(number -> numberCounts.put(number, 0L));

        Map<String, Long> oddEvenRatios = new LinkedHashMap<>();
        Map<String, Long> sumRanges = new LinkedHashMap<>();
        sumRanges.put("90 미만", 0L);
        sumRanges.put("90~119", 0L);
        sumRanges.put("120~150", 0L);
        sumRanges.put("151 이상", 0L);

        for (WinningDraw draw : draws) {
            // 회차별 6개 번호를 순회하면서 번호 빈도, 홀짝 비율, 합계 구간을 누적합니다.
            for (Integer number : draw.getNumbers()) {
                numberCounts.compute(number, (key, value) -> value == null ? 1L : value + 1L);
            }
            long oddCount = draw.getNumbers().stream().filter(number -> number % 2 != 0).count();
            String ratio = oddCount + ":" + (6 - oddCount);
            oddEvenRatios.compute(ratio, (key, value) -> value == null ? 1L : value + 1L);

            int sum = draw.getNumbers().stream().mapToInt(Integer::intValue).sum();
            String range = sum < 90 ? "90 미만" : sum < 120 ? "90~119" : sum <= 150 ? "120~150" : "151 이상";
            sumRanges.compute(range, (key, value) -> value == null ? 1L : value + 1L);
        }

        List<NumberStat> stats = numberCounts.entrySet().stream()
                .map(entry -> new NumberStat(entry.getKey(), entry.getValue()))
                .toList();
        long maxNumberCount = stats.stream()
                .mapToLong(NumberStat::getCount)
                .max()
                .orElse(1L);

        return new StatsSummary(
                draws.size(),
                stats,
                stats.stream().sorted(Comparator.comparingLong(NumberStat::getCount).reversed()).limit(10).toList(),
                stats.stream().sorted(Comparator.comparingLong(NumberStat::getCount)).limit(10).toList(),
                oddEvenRatios,
                sumRanges,
                maxNumberCount
        );
    }
}
