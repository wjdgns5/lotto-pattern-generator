package com.example.lotto.repository;

import com.example.lotto.domain.WinningDrawEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WinningDrawRepository extends JpaRepository<WinningDrawEntity, Integer> {

    List<WinningDrawEntity> findTop10ByOrderByDrawNumberDesc();

    Optional<WinningDrawEntity> findTopByOrderByDrawNumberDesc();
}
