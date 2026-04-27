package com.example.lotto.repository;

import com.example.lotto.domain.RulePreset;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RulePresetRepository extends JpaRepository<RulePreset, Long> {

    List<RulePreset> findByUsernameOrderByCreatedAtDesc(String username);
}
