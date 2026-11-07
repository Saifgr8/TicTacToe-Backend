package com.example.TicTacToe.Repositories;

import com.example.TicTacToe.Models.Match;
import com.example.TicTacToe.Models.Player;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MatchRepository extends JpaRepository<Match, Long> {
    Optional<List<Match>> findAllByPlayer1_idOrPlayer2_id(Long player1Id, Long player2Id);
}
