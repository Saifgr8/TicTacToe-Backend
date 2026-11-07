package com.example.TicTacToe.WebSocketConfiguration;

import com.example.TicTacToe.DTOs.GameDTOs.DTOGameCreate;
import com.example.TicTacToe.GAME.GameEngine;
import com.example.TicTacToe.GAME.GameSession;
import com.example.TicTacToe.GlobalException.GameExceptions.SessionCreationException;
import com.example.TicTacToe.GlobalException.PlayerNotFoundException;
import com.example.TicTacToe.Models.Enums.MatchStatus;
import com.example.TicTacToe.Models.Enums.PlayerStatus;
import com.example.TicTacToe.Models.Match;
import com.example.TicTacToe.Models.Player;
import com.example.TicTacToe.Repositories.MatchDataRepository;
import com.example.TicTacToe.Repositories.MatchRepository;
import com.example.TicTacToe.Repositories.PlayerRepositories;
import com.example.TicTacToe.Utils.JsonToString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class GameSessionManager {
    @Autowired
    private PlayerRepositories playerRepositories;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private JsonToString jsonToString;

    @Autowired
    private MatchDataRepository matchDataRepository;

    @Autowired
    private GameEngine gameEngine;

    private final Map<String, GameSession> activeSessions = new ConcurrentHashMap<>();

    public GameSession createSession(DTOGameCreate dto) {
        System.out.println(dto);
        //FIND PLAYERS
        Player player1 = playerRepositories.findPlayerByUsername(dto.getPlayer1Username())
                .orElseThrow(() -> new PlayerNotFoundException("No player found with this username"));
        Player player2 = playerRepositories.findPlayerByUsername(dto.getPlayer2Username())
                .orElseGet(() -> {
                    System.out.println("⚠️ Player2 not found, creating a new dummy player: " + dto.getPlayer2Username());
                   //String uniqueId = UUID.randomUUID().toString().substring(0, 8); // short random ID
                    String uniqueUsername = dto.getPlayer2Username();
                    String uniqueEmail = uniqueUsername + "@dummy.com";
                    Player dummy = new Player(
                            uniqueEmail,
                            uniqueUsername,
                            "password123"
                    );
                    dummy.setPlayerStatus(PlayerStatus.ONLINE);
                    dummy.setBehaviourScore(100);
                    dummy.setCreatedOn(LocalDateTime.now());
                    return playerRepositories.save(dummy);
                });

        //CREATE MATCH
        Match match = new Match();
        match.setBoardSize(dto.getBoardSize());
        match.setPlayer1(player1);
        match.setPlayer2(player2);
        match.setStatus(MatchStatus.ONGOING);
        match.setStartedAt(LocalDateTime.now());
        //SAVE MATCH
        Match savedMatch = matchRepository.save(match);

        //CREATE SESSION
        GameSession gameSession = new GameSession(
                dto.getBoardSize(),
                dto.getPlayer1Username(),
                dto.getPlayer2Username(),
                dto.getPlayer1Symbol(),
                dto.getPlayer2Symbol(),
                jsonToString,
                matchDataRepository,
                gameEngine,
                playerRepositories,
                matchRepository,
                dto.getStartingPlayer()
        );
        gameSession.setMatch(savedMatch);

        //SAVE PLAYER CHANGE
        player1.setPlayerStatus(PlayerStatus.PLAYING);
        player2.setPlayerStatus(PlayerStatus.PLAYING);
        playerRepositories.save(player1);
        playerRepositories.save(player2);

        activeSessions.put(gameSession.getSessionId(), gameSession);
        System.out.println("Game session managher: " + activeSessions);

        return gameSession;
    }

    public GameSession getSession(String sessionId) {
        return activeSessions.get(sessionId);
    }
}
