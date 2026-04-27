package com.example.lotto.service;

import com.example.lotto.domain.RulePreset;
import com.example.lotto.model.GenerationRequest;
import com.example.lotto.model.PresetRequest;
import com.example.lotto.repository.RulePresetRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RulePresetService {

    private final RulePresetRepository rulePresetRepository;

    public RulePresetService(RulePresetRepository rulePresetRepository) {
        this.rulePresetRepository = rulePresetRepository;
    }

    @Transactional(readOnly = true)
    public List<RulePreset> findMine(String username) {
        return rulePresetRepository.findByUsernameOrderByCreatedAtDesc(username);
    }

    @Transactional(readOnly = true)
    public RulePreset findMine(Long id, String username) {
        return rulePresetRepository.findById(id)
                .filter(item -> item.getUsername().equals(username))
                .orElseThrow(() -> new IllegalArgumentException("프리셋을 찾을 수 없습니다."));
    }

    @Transactional(readOnly = true)
    public GenerationRequest toRequest(Long id, String username) {
        RulePreset preset = findMine(id, username);
        GenerationRequest request = new GenerationRequest();
        request.setThreeOddThreeEvenCount(preset.getThreeOddThreeEvenCount());
        request.setFourOddTwoEvenCount(preset.getFourOddTwoEvenCount());
        request.setMinSum(preset.getMinSum());
        request.setMaxSum(preset.getMaxSum());
        request.setExcludedNumbers(preset.getExcludedNumbers());
        return request;
    }

    @Transactional
    public void save(String username, PresetRequest request) {
        String name = request.getName() == null ? "" : request.getName().trim();
        if (name.isBlank()) {
            throw new IllegalArgumentException("프리셋 이름을 입력해 주세요.");
        }
        rulePresetRepository.save(new RulePreset(
                name,
                username,
                request.getThreeOddThreeEvenCount(),
                request.getFourOddTwoEvenCount(),
                request.getMinSum(),
                request.getMaxSum(),
                request.getExcludedNumbers()
        ));
    }

    @Transactional
    public void deleteMine(Long id, String username) {
        RulePreset preset = findMine(id, username);
        rulePresetRepository.delete(preset);
    }
}
