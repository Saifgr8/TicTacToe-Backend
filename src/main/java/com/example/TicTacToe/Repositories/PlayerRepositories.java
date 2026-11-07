package com.example.TicTacToe.Repositories;

import com.example.TicTacToe.Models.Player;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlayerRepositories extends JpaRepository<Player, Long> {
    Optional<Player> findPlayerByEmail(String email);
    Optional<Player> findPlayerByUsername(String username);
    Optional<Player> findPlayerById(Long id);
}
