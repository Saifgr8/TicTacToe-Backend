package com.example.TicTacToe.Repositories;

import com.example.TicTacToe.Models.MatchData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MatchDataRepository extends JpaRepository<MatchData, Long> {
    Optional<List<MatchData>> findByMatchIdOrderByIdAsc(Long matchId);
}
