package com.example.lotto.repository;

import com.example.lotto.domain.GenerationHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GenerationHistoryRepository extends JpaRepository<GenerationHistory, Long> {

    List<GenerationHistory> findTop3ByUsernameOrderByCreatedAtDesc(String username);

    List<GenerationHistory> findByUsernameOrderByCreatedAtDesc(String username);
}
