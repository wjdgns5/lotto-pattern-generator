package com.example.lotto.service;

import com.example.lotto.model.GenerationRequest;
import com.example.lotto.model.GenerationResult;
import com.example.lotto.model.LottoCandidate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class LottoGenerationService {

    private static final Set<Integer> LOW_FREQUENCY_NUMBERS = Set.of(4, 10, 20, 22, 26, 32, 34, 40, 42);
    private static final Set<String> THREE_THREE_PATTERNS = Set.of("OEOEOE", "EOEOEO", "OEOEEO", "OEEOEO");
    private static final Set<String> FOUR_TWO_PATTERNS = Set.of("OEOEOO", "OEOOEO", "OOEOEO");
    private static final int MAX_ATTEMPTS_PER_GROUP = 20_000;

    private final SecureRandom random = new SecureRandom();
    private final WinningNumberService winningNumberService;

    public LottoGenerationService(WinningNumberService winningNumberService) {
        this.winningNumberService = winningNumberService;
    }

    public GenerationResult generate(GenerationRequest request) {
        validate(request);

        Set<Integer> excludedNumbers = parseExcludedNumbers(request.getExcludedNumbers());
        List<String> messages = new ArrayList<>();
        Set<String> generatedKeys = new HashSet<>();
        Set<String> historicalWinningKeys = winningNumberService.findAll().stream()
                .map(draw -> draw.getNumbers().toString())
                .collect(Collectors.toSet());

        List<LottoCandidate> threeThree = generateGroup(
                request.getThreeOddThreeEvenCount(),
                3,
                3,
                THREE_THREE_PATTERNS,
                request,
                excludedNumbers,
                generatedKeys,
                historicalWinningKeys
        );
        addShortageMessage(messages, "3:3", request.getThreeOddThreeEvenCount(), threeThree.size());

        List<LottoCandidate> fourTwo = generateGroup(
                request.getFourOddTwoEvenCount(),
                4,
                2,
                FOUR_TWO_PATTERNS,
                request,
                excludedNumbers,
                generatedKeys,
                historicalWinningKeys
        );
        addShortageMessage(messages, "4:2", request.getFourOddTwoEvenCount(), fourTwo.size());

        if (!historicalWinningKeys.isEmpty()) {
            messages.add("누적 당첨번호 " + historicalWinningKeys.size() + "회차와 완전히 같은 조합은 후보에서 제외했습니다.");
        }
        if (messages.isEmpty()) {
            messages.add("요청한 조건을 만족하는 패턴 기반 후보를 생성했습니다.");
        }

        return new GenerationResult(threeThree, fourTwo, messages);
    }

    private List<LottoCandidate> generateGroup(
            int count,
            int oddCount,
            int evenCount,
            Set<String> allowedPatterns,
            GenerationRequest request,
            Set<Integer> excludedNumbers,
            Set<String> generatedKeys,
            Set<String> historicalWinningKeys
    ) {
        List<LottoCandidate> candidates = new ArrayList<>();

        for (int attempt = 0; attempt < MAX_ATTEMPTS_PER_GROUP && candidates.size() < count; attempt++) {
            List<Integer> numbers = randomNumbers(excludedNumbers);
            LottoCandidate candidate = toCandidate(numbers);
            String key = candidate.getNumbers().toString();

            if (!hasOddEvenRatio(numbers, oddCount, evenCount)) {
                continue;
            }
            if (!allowedPatterns.contains(candidate.getOddEvenPattern())) {
                continue;
            }
            if (candidate.getSum() < request.getMinSum() || candidate.getSum() > request.getMaxSum()) {
                continue;
            }
            if (candidate.getLowFrequencyNumbers().size() > 1) {
                continue;
            }
            if (!passesMarkerRules(candidate.getNumbers())) {
                continue;
            }
            if (historicalWinningKeys.contains(key)) {
                continue;
            }

            if (generatedKeys.add(key)) {
                candidates.add(candidate);
            }
        }

        return candidates;
    }

    private List<Integer> randomNumbers(Set<Integer> excludedNumbers) {
        List<Integer> pool = IntStream.rangeClosed(1, 45)
                .filter(number -> !excludedNumbers.contains(number))
                .boxed()
                .collect(Collectors.toCollection(ArrayList::new));
        Collections.shuffle(pool, random);
        return pool.stream()
                .limit(6)
                .sorted()
                .toList();
    }

    private LottoCandidate toCandidate(List<Integer> numbers) {
        int sum = numbers.stream().mapToInt(Integer::intValue).sum();
        String pattern = numbers.stream()
                .map(number -> number % 2 == 0 ? "E" : "O")
                .collect(Collectors.joining());
        List<Integer> lowFrequencyNumbers = numbers.stream()
                .filter(LOW_FREQUENCY_NUMBERS::contains)
                .toList();

        return new LottoCandidate(numbers, sum, pattern, lowFrequencyNumbers);
    }

    private boolean hasOddEvenRatio(List<Integer> numbers, int oddCount, int evenCount) {
        long actualOddCount = numbers.stream().filter(number -> number % 2 != 0).count();
        long actualEvenCount = numbers.size() - actualOddCount;
        return actualOddCount == oddCount && actualEvenCount == evenCount;
    }

    private boolean passesMarkerRules(List<Integer> numbers) {
        return hasAtMostTwoInSameColumn(numbers)
                && hasNoThreeStepSevenSequence(numbers)
                && hasAtMostThreeInSameRow(numbers);
    }

    private boolean hasAtMostTwoInSameColumn(List<Integer> numbers) {
        int[] counts = new int[7];
        for (int number : numbers) {
            counts[(number - 1) % 7]++;
        }
        for (int count : counts) {
            if (count > 2) {
                return false;
            }
        }
        return true;
    }

    private boolean hasNoThreeStepSevenSequence(List<Integer> numbers) {
        Set<Integer> numberSet = new HashSet<>(numbers);
        for (int number : numbers) {
            if (numberSet.contains(number + 7) && numberSet.contains(number + 14)) {
                return false;
            }
        }
        return true;
    }

    private boolean hasAtMostThreeInSameRow(List<Integer> numbers) {
        int[] counts = new int[7];
        for (int number : numbers) {
            counts[(number - 1) / 7]++;
        }
        for (int count : counts) {
            if (count > 3) {
                return false;
            }
        }
        return true;
    }

    private Set<Integer> parseExcludedNumbers(String rawExcludedNumbers) {
        Set<Integer> excludedNumbers = new HashSet<>();
        if (rawExcludedNumbers == null || rawExcludedNumbers.isBlank()) {
            return excludedNumbers;
        }

        String[] tokens = rawExcludedNumbers.split("[,\\s]+");
        for (String token : tokens) {
            if (token.isBlank()) {
                continue;
            }
            int number;
            try {
                number = Integer.parseInt(token);
            } catch (NumberFormatException exception) {
                throw new IllegalArgumentException("제외수는 숫자만 입력할 수 있습니다: " + token);
            }
            if (number < 1 || number > 45) {
                throw new IllegalArgumentException("제외수는 1부터 45 사이여야 합니다: " + number);
            }
            excludedNumbers.add(number);
        }
        if (excludedNumbers.size() > 39) {
            throw new IllegalArgumentException("후보 생성을 위해 제외수는 최대 39개까지만 입력할 수 있습니다.");
        }
        return excludedNumbers;
    }

    private void validate(GenerationRequest request) {
        if (request.getThreeOddThreeEvenCount() < 1 || request.getThreeOddThreeEvenCount() > 5) {
            throw new IllegalArgumentException("3:3 개수는 1게임부터 5게임까지 입력할 수 있습니다.");
        }
        if (request.getFourOddTwoEvenCount() < 1 || request.getFourOddTwoEvenCount() > 5) {
            throw new IllegalArgumentException("4:2 개수는 1게임부터 5게임까지 입력할 수 있습니다.");
        }
        if (request.getThreeOddThreeEvenCount() < 0 || request.getFourOddTwoEvenCount() < 0) {
            throw new IllegalArgumentException("생성 개수는 0 이상이어야 합니다.");
        }
        if (request.getThreeOddThreeEvenCount() + request.getFourOddTwoEvenCount() > 100) {
            throw new IllegalArgumentException("한 번에 생성할 수 있는 후보는 최대 100개입니다.");
        }
        if (request.getMinSum() < 21 || request.getMaxSum() > 255 || request.getMinSum() > request.getMaxSum()) {
            throw new IllegalArgumentException("합계 범위를 올바르게 입력해 주세요.");
        }
    }

    private void addShortageMessage(List<String> messages, String groupName, int requestedCount, int actualCount) {
        if (actualCount < requestedCount) {
            messages.add(groupName + " 조건에서 요청한 " + requestedCount + "개 중 "
                    + actualCount + "개만 생성했습니다. 조건을 조금 완화해 보세요.");
        }
    }
}
