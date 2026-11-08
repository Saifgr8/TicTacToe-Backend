package com.example.TicTacToe.Services.MatchService;

import com.example.TicTacToe.DTOs.GameDTOs.DTOGameMove;
import com.example.TicTacToe.DTOs.GameDTOs.DTOGameSessionResponse;
import com.example.TicTacToe.Contollers.MatchContoller.MatchController;
import com.example.TicTacToe.GAME.Enums.GameModes;
import com.example.TicTacToe.GAME.Enums.MoveType;
import com.example.TicTacToe.GAME.Enums.PatternType;
import com.example.TicTacToe.GAME.GameEngine;
import com.example.TicTacToe.GAME.GameSession;
import com.example.TicTacToe.GAME.UTILS.GetAllAntiDiagonalPairs;
import com.example.TicTacToe.GAME.UTILS.GetAllDiagonalMain;
import com.example.TicTacToe.GAME.UTILS.GetAllDiagonalPairsWithoutMain;
import com.example.TicTacToe.GlobalException.GameExceptions.InvalidSessionException;
import com.example.TicTacToe.GlobalException.UnauthorizedRequestException;
import com.example.TicTacToe.Models.Match;
import com.example.TicTacToe.Models.Player;
import com.example.TicTacToe.Repositories.MatchDataRepository;
import com.example.TicTacToe.Repositories.MatchRepository;
import com.example.TicTacToe.Repositories.PlayerRepositories;
import com.example.TicTacToe.Utils.JsonToString;
import com.example.TicTacToe.WebSocketConfiguration.GameSessionManager;
import com.example.TicTacToe.bot.BotEngine;
import com.example.TicTacToe.bot.BotTurnScheduler;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class MatchSessionPollingTest {
    @Test
    void pollingEndpointReturnsMoveDetails() throws Exception {
        MatchService service = mock(MatchService.class);
        DTOGameSessionResponse response = new DTOGameSessionResponse();
        response.setBotThinking(false);
        response.setLastMovePlayer("bot");
        response.setLastMove(new DTOGameMove("session", 1, 2, "O", MoveType.PATTERN_BOMB, PatternType.ROW));
        when(service.getSessionState("session", "human@example.com")).thenReturn(response);
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new MatchController(service)).build();

        mvc.perform(get("/api/match/session/session").principal(
                        new UsernamePasswordAuthenticationToken("human@example.com", "password", List.of())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.botThinking").value(false))
                .andExpect(jsonPath("$.lastMovePlayer").value("bot"))
                .andExpect(jsonPath("$.lastMove.x").value(1))
                .andExpect(jsonPath("$.lastMove.y").value(2))
                .andExpect(jsonPath("$.lastMove.moveType").value("PATTERN_BOMB"))
                .andExpect(jsonPath("$.lastMove.patternType").value("ROW"));
    }

    @Test
    void pollingReturnsUpdatedSnapshotOnlyToMatchPlayers() {
        GameSessionManager manager = mock(GameSessionManager.class);
        BotTurnScheduler scheduler = mock(BotTurnScheduler.class);
        MatchService service = new MatchService(mock(MatchRepository.class), mock(PlayerRepositories.class),
                manager, scheduler);
        GameSession session = new GameSession(3, "human", "bot", "X", "O", mock(JsonToString.class),
                mock(MatchDataRepository.class),
                new GameEngine(new GetAllDiagonalPairsWithoutMain(), new GetAllAntiDiagonalPairs(),
                        new GetAllDiagonalMain()), mock(PlayerRepositories.class), mock(MatchRepository.class),
                "human", GameModes.BOTMATCH, new BotEngine());
        Match match = new Match();
        match.setPlayer1(new Player("human@example.com", "human", "password"));
        match.setPlayer2(new Player("bot@example.com", "bot", "password"));
        session.setMatch(match);
        when(manager.getSession(session.getSessionId())).thenReturn(session);

        DTOGameSessionResponse humanMove = service.makeMove(new DTOGameMove(
                session.getSessionId(), 0, 0, "X", MoveType.NORMAL, PatternType.NONE));
        assertTrue(humanMove.isBotThinking());
        verify(scheduler).scheduleAfterCommit(session);
        session.playBotMoveIfNeeded();

        DTOGameSessionResponse polled = service.getSessionState(session.getSessionId(), "human@example.com");
        assertFalse(polled.isBotThinking());
        assertEquals("bot", polled.getLastMovePlayer());
        assertEquals("O", polled.getLastMove().getSymbol());
        assertThrows(UnauthorizedRequestException.class,
                () -> service.getSessionState(session.getSessionId(), "other@example.com"));
        assertThrows(InvalidSessionException.class,
                () -> service.getSessionState("missing", "human@example.com"));
    }
}
