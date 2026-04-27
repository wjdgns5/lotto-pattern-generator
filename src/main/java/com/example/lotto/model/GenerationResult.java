package com.example.lotto.model;

import java.util.List;

public class GenerationResult {

    private final List<LottoCandidate> threeOddThreeEvenCandidates;
    private final List<LottoCandidate> fourOddTwoEvenCandidates;
    private final List<String> messages;

    public GenerationResult(
            List<LottoCandidate> threeOddThreeEvenCandidates,
            List<LottoCandidate> fourOddTwoEvenCandidates,
            List<String> messages
    ) {
        this.threeOddThreeEvenCandidates = threeOddThreeEvenCandidates;
        this.fourOddTwoEvenCandidates = fourOddTwoEvenCandidates;
        this.messages = messages;
    }

    public List<LottoCandidate> getThreeOddThreeEvenCandidates() {
        return threeOddThreeEvenCandidates;
    }

    public List<LottoCandidate> getFourOddTwoEvenCandidates() {
        return fourOddTwoEvenCandidates;
    }

    public List<String> getMessages() {
        return messages;
    }
}
