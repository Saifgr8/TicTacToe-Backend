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
import com.example.TicTacToe.bot.BotEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class GameSessionManager {
    private static final Logger log = LoggerFactory.getLogger(GameSessionManager.class);
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

    @Autowired
    private BotEngine botEngine;

    private final Map<String, GameSession> activeSessions = new ConcurrentHashMap<>();

    public GameSession createSession(DTOGameCreate dto) {
        //FIND PLAYERS
        Player player1 = playerRepositories.findPlayerByUsername(dto.getPlayer1Username())
                .orElseThrow(() -> new PlayerNotFoundException("No player found with this username"));
        Player player2 = playerRepositories.findPlayerByEmail(dto.getPlayer2Username() + "@dummy.com")
                .orElseGet(() -> {
                    log.debug("Creating dummy second player for new session");
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
                dto.getStartingPlayer(),
                dto.getGameMode(),
                botEngine
        );
        gameSession.setMatch(savedMatch);

        //SAVE PLAYER CHANGE
        player1.setPlayerStatus(PlayerStatus.PLAYING);
        playerRepositories.save(player1);
        player2.setPlayerStatus(PlayerStatus.PLAYING);
        playerRepositories.save(player2);

        activeSessions.put(gameSession.getSessionId(), gameSession);
        log.info("Game session created: session={}, mode={}, boardSize={}",
                gameSession.getSessionId(), dto.getGameMode(), dto.getBoardSize());

        return gameSession;
    }

    public GameSession getSession(String sessionId) {
        return activeSessions.get(sessionId);
    }
}
