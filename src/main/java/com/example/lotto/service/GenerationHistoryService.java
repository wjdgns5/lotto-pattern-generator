package com.example.lotto.service;

import com.example.lotto.domain.GenerationHistory;
import com.example.lotto.model.GenerationRequest;
import com.example.lotto.model.GenerationResult;
import com.example.lotto.model.LottoCandidate;
import com.example.lotto.repository.GenerationHistoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class GenerationHistoryService {

    private static final int MAX_HISTORY_PER_USER = 3;

    private final GenerationHistoryRepository generationHistoryRepository;

    public GenerationHistoryService(GenerationHistoryRepository generationHistoryRepository) {
        this.generationHistoryRepository = generationHistoryRepository;
    }

    @Transactional
    public void save(String username, GenerationRequest request, GenerationResult result) {
        generationHistoryRepository.save(new GenerationHistory(
                username,
                requestSummary(request),
                resultText(result)
        ));
        trimOldHistories(username);
    }

    @Transactional(readOnly = true)
    public List<GenerationHistory> findMine(String username) {
        return generationHistoryRepository.findTop3ByUsernameOrderByCreatedAtDesc(username);
    }

    private void trimOldHistories(String username) {
        List<GenerationHistory> histories = generationHistoryRepository.findByUsernameOrderByCreatedAtDesc(username);
        if (histories.size() <= MAX_HISTORY_PER_USER) {
            return;
        }
        generationHistoryRepository.deleteAll(histories.subList(MAX_HISTORY_PER_USER, histories.size()));
    }

    private String requestSummary(GenerationRequest request) {
        return "3:3 " + request.getThreeOddThreeEvenCount()
                + "개, 4:2 " + request.getFourOddTwoEvenCount()
                + "개, 합계 " + request.getMinSum() + "~" + request.getMaxSum()
                + ", 제외수 " + (request.getExcludedNumbers() == null || request.getExcludedNumbers().isBlank()
                ? "없음"
                : request.getExcludedNumbers());
    }

    private String resultText(GenerationResult result) {
        return Stream.concat(
                        result.getThreeOddThreeEvenCandidates().stream(),
                        result.getFourOddTwoEvenCandidates().stream()
                )
                .map(LottoCandidate::getCopyText)
                .collect(Collectors.joining("\n"));
    }
}
