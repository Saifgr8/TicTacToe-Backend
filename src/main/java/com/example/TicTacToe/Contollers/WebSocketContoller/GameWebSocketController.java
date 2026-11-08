package com.example.TicTacToe.Contollers.WebSocketContoller;

import com.example.TicTacToe.DTOs.GameDTOs.*;
import com.example.TicTacToe.GAME.GameSession;
import com.example.TicTacToe.GAME.GameSessionSnapshot;
import com.example.TicTacToe.GlobalException.GameExceptions.MoveValidationFailException;
import com.example.TicTacToe.Utils.RPSWinner;
import com.example.TicTacToe.WebSocketConfiguration.GameSessionManager;
import com.example.TicTacToe.bot.BotTurnScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.security.Principal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Controller
public class GameWebSocketController {
    private static final Logger log = LoggerFactory.getLogger(GameWebSocketController.class);
    @Autowired
    private GameSessionManager gameSessionManager;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private BotTurnScheduler botTurnScheduler;

    private final Map<String, Map<String, String>> rpsChoicesMap = new ConcurrentHashMap<>();

    @Autowired
    private RPSWinner rpsWinner;

    //CREATE THE GAME;
    @MessageMapping("/create")
    @SendTo("/topic/session")
    public DTOGameSessionResponse createGame(@Payload DTOGameCreate dto) {

        GameSession session = gameSessionManager.createSession(dto);
        DTOGameSessionResponse response = GameSessionSnapshot.from(session);
        botTurnScheduler.scheduleAfterCommit(session);
        return response;
    }

    //HANDLE MOVE
    @MessageMapping("/move")
    @SendTo("/topic/game")
    public void handleMove(@Payload DTOGameMove dto) {
        GameSession session = gameSessionManager.getSession(dto.getSessionId());
        if (session != null) {
            synchronized (session) {
                boolean success = session.processMove(
                        dto.getX(), dto.getY(), dto.getSymbol(), dto.getMoveType(), dto.getPatternType()
                );
                if (!success) {
                    throw new MoveValidationFailException("Invalid Move");
                }
                DTOGameSessionResponse response = GameSessionSnapshot.from(session);
                messagingTemplate.convertAndSend("/topic/game/" + dto.getSessionId(), response);
                botTurnScheduler.scheduleAfterCommit(session);
            }

        } else {
            log.debug("WebSocket move rejected: session not found");
            messagingTemplate.convertAndSend("/topic/errors", "Invalid session ID: " + dto.getSessionId());
        }
    }

    @MessageMapping("/rps/choice")
    public void handleRSPChoice(@Payload DTORPSChoice dtorpsChoice, Principal principal) {

        String currentUsername = (principal.getName());
        String matchId = dtorpsChoice.getRpsMatchId();

        // Store the choice for this specific match ID
        rpsChoicesMap.computeIfAbsent(matchId, k -> new ConcurrentHashMap<>())
                .put(currentUsername, dtorpsChoice.getChoice());


        String player1 = (dtorpsChoice.getPlayer1());
        String player2 = (dtorpsChoice.getPlayer2());

        String player1Choice = rpsChoicesMap.get(matchId).get(player1);
        String player2Choice = rpsChoicesMap.get(matchId).get(player2);

        if (player1Choice != null && player2Choice != null) {
            String winner = rpsWinner.determineRPSWinner(player1Choice, player2Choice, player1, player2);

            DTORPSResult resultPlayer1 = new DTORPSResult(dtorpsChoice.getPlayer1(), dtorpsChoice.getPlayer2(), player1Choice, player2Choice, winner,
                    winner.equals(player1) ? "WIN" : winner.equals("DRAW") ? "DRAW" : "LOOSE", matchId);
            DTORPSResult resultPlayer2 = new DTORPSResult(dtorpsChoice.getPlayer1(), dtorpsChoice.getPlayer2(), player1Choice, player2Choice, winner,
                    winner.equals(player2) ? "WIN" : winner.equals("DRAW") ? "DRAW" : "LOOSE", matchId);

            messagingTemplate.convertAndSendToUser(dtorpsChoice.getPlayer1(), "/queue/rps-result", resultPlayer1);
            messagingTemplate.convertAndSendToUser(dtorpsChoice.getPlayer2(), "/queue/rps-result", resultPlayer2);

            rpsChoicesMap.remove(matchId);
        }
    }

    @MessageMapping("rps/winner")
    public void handleRPSWinner(@Payload DTORPSWinner dtorpsWinner, Principal principal) {
        String currentWinner = principal.getName();
        String dtoWinnerPlayer = dtorpsWinner.getWinnerPlayer();

        if (!currentWinner.equalsIgnoreCase(dtoWinnerPlayer)) {
            throw new RuntimeException("Winner players are different.");
        }

        messagingTemplate.convertAndSendToUser(dtorpsWinner.getLooserPlayer(), "/queue/rps-winner", dtorpsWinner);
        messagingTemplate.convertAndSendToUser(currentWinner, "/queue/rps-winner", dtorpsWinner);
    }

}