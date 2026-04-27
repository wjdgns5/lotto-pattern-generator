package com.example.lotto.service;

import com.example.lotto.model.GenerationRequest;
import com.example.lotto.model.GenerationResult;
import com.example.lotto.model.LottoCandidate;
import com.example.lotto.model.WinningNumberUpdateRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:lotto-test;MODE=MySQL;DATABASE_TO_LOWER=TRUE",
        "app.lotto.seed-csv=data/winning-numbers.csv",
        "app.lotto.auto-update.enabled=false"
})
class LottoGenerationServiceTests {

    @Autowired
    private LottoGenerationService service;

    @Autowired
    private WinningNumberService winningNumberService;

    @Test
    void generatesDefaultCandidatesThatMatchRules() {
        GenerationRequest request = new GenerationRequest();

        GenerationResult result = service.generate(request);

        assertThat(result.getThreeOddThreeEvenCandidates()).hasSize(5);
        assertThat(result.getFourOddTwoEvenCandidates()).hasSize(5);
        assertCandidates(result.getThreeOddThreeEvenCandidates(), 3, 3);
        assertCandidates(result.getFourOddTwoEvenCandidates(), 4, 2);
    }

    @Test
    void rejectsInvalidExcludedNumber() {
        GenerationRequest request = new GenerationRequest();
        request.setExcludedNumbers("1, 46");

        assertThatThrownBy(() -> service.generate(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("1부터 45 사이");
    }

    @Test
    void rejectsGameCountOutsideOneToFive() {
        GenerationRequest request = new GenerationRequest();
        request.setThreeOddThreeEvenCount(6);

        assertThatThrownBy(() -> service.generate(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("3:3 개수는 1게임부터 5게임까지");
    }

    @Test
    void savesWinningNumbersForHistoricalExclusion() {
        WinningNumberUpdateRequest request = new WinningNumberUpdateRequest();
        request.setDrawNumber(9000);
        request.setDrawDate("2026-04-27");
        request.setNumber1(10);
        request.setNumber2(23);
        request.setNumber3(29);
        request.setNumber4(33);
        request.setNumber5(37);
        request.setNumber6(40);
        request.setBonusNumber(16);

        winningNumberService.saveOrUpdate(request);

        assertThat(winningNumberService.isHistoricalWinningCombination(List.of(40, 37, 33, 29, 23, 10))).isTrue();
    }

    private void assertCandidates(List<LottoCandidate> candidates, int oddCount, int evenCount) {
        Set<Integer> lowFrequencyNumbers = Set.of(4, 10, 20, 22, 26, 32, 34, 40, 42);

        for (LottoCandidate candidate : candidates) {
            assertThat(candidate.getNumbers()).hasSize(6).isSorted();
            assertThat(candidate.getNumbers()).allMatch(number -> number >= 1 && number <= 45);
            assertThat(candidate.getNumbers()).doesNotHaveDuplicates();
            assertThat(candidate.getSum()).isBetween(120, 150);
            assertThat(candidate.getNumbers().stream().filter(number -> number % 2 != 0).count()).isEqualTo(oddCount);
            assertThat(candidate.getNumbers().stream().filter(number -> number % 2 == 0).count()).isEqualTo(evenCount);
            assertThat(candidate.getNumbers().stream().filter(lowFrequencyNumbers::contains).count()).isLessThanOrEqualTo(1);
        }
    }
}
