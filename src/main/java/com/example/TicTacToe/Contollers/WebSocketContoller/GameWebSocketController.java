package com.example.TicTacToe.Contollers.WebSocketContoller;

import com.example.TicTacToe.DTOs.GameDTOs.*;
import com.example.TicTacToe.GAME.GameSession;
import com.example.TicTacToe.Utils.RPSWinner;
import com.example.TicTacToe.WebSocketConfiguration.GameSessionManager;
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
    @Autowired
    private GameSessionManager gameSessionManager;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    private final Map<String, Map<String, String>> rpsChoicesMap = new ConcurrentHashMap<>();

    @Autowired
    private RPSWinner rpsWinner;

    //CREATE THE GAME;
    @MessageMapping("/create")
    @SendTo("/topic/session")
    public DTOGameSessionResponse createGame(@Payload DTOGameCreate dto) {

        System.out.println("🔥 CREATE GAME method triggered with: " + dto.getPlayer1Username());
        GameSession session = gameSessionManager.createSession(dto);
        System.out.println(session.getSessionId());
        DTOGameSessionResponse response = convertToDTO(session);
        return response;
    }

    //HANDLE MOVE
    @MessageMapping("/move")
    @SendTo("/topic/game")
    public void handleMove(@Payload DTOGameMove dto) {
        System.out.println("Move is triggered: " + dto.getSessionId());
        GameSession session = gameSessionManager.getSession(dto.getSessionId());
        System.out.println(session);
        if (session != null) {
            boolean success = session.processMove(
                    dto.getX(), dto.getY(), dto.getSymbol(), dto.getMoveType(), dto.getPatternType()
            );
            DTOGameSessionResponse response = convertToDTO(session);
            System.out.println("after a move was made, response is: " + response.toString());
            messagingTemplate.convertAndSend("/topic/game/" + dto.getSessionId(), response);

        } else {
            messagingTemplate.convertAndSend("/topic/errors", "Invalid session ID: " + dto.getSessionId());
        }
    }

    @MessageMapping("/rps/choice")
    public void handleRSPChoice(@Payload DTORPSChoice dtorpsChoice, Principal principal) {

        System.out.println(dtorpsChoice.toString());
        String currentUsername = (principal.getName());
        String matchId = dtorpsChoice.getRpsMatchId();

        // Store the choice for this specific match ID
        rpsChoicesMap.computeIfAbsent(matchId, k -> new ConcurrentHashMap<>())
                .put(currentUsername, dtorpsChoice.getChoice());

        System.out.println("MAP IS: " + rpsChoicesMap);

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

        System.out.println(currentWinner);
        System.out.println("from dto: " + dtoWinnerPlayer);
        if (!currentWinner.equalsIgnoreCase(dtoWinnerPlayer)) {
            throw new RuntimeException("Winner players are different.");
        }

        messagingTemplate.convertAndSendToUser(dtorpsWinner.getLooserPlayer(), "/queue/rps-winner", dtorpsWinner);
        messagingTemplate.convertAndSendToUser(currentWinner, "/queue/rps-winner", dtorpsWinner);
    }

    private DTOGameSessionResponse convertToDTO(GameSession session) {
        DTOGameSessionResponse response = new DTOGameSessionResponse();
        //GAME REALTED
        response.setSessionId(session.getSessionId());
        response.setBoard(session.getBoard());
        response.setGameStatus(session.getGameStatus());
        response.setCurrentPlayer(session.getCurrentPlayer());
        response.setWinner(session.getWinner());
        response.setDoubleOverrideActive(session.isDoubleOverride());
        response.setDoubleOverrideTurnsLeft(session.getDoubleOverrideTurns());

        //PLAYER REALTED
        response.setPlayer1Points(session.getPlayer1Points());
        response.setPlayer1Symbol(session.getPlayer1Symbol());
        response.setPlayer1TwistTurns(session.getPlayer1TwistTurnsLeft());
        response.setPlayer2Points(session.getPlayer2Points());
        response.setPlayer2Symbol(session.getPlayer2Symbol());
        response.setPlayer2TwistTurns(session.getPlayer2TwistTurnsLeft());
        response.setTwistActive(session.isTwistActive());
        return response;
    }


}