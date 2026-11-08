package com.example.TicTacToe.GAME;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.example.TicTacToe.DTOs.GameDTOs.DTOGameSessionResponse;
import com.example.TicTacToe.GAME.Enums.GameModes;
import com.example.TicTacToe.GAME.Enums.MoveType;
import com.example.TicTacToe.GAME.Enums.PatternType;
import com.example.TicTacToe.GAME.UTILS.GetAllAntiDiagonalPairs;
import com.example.TicTacToe.GAME.UTILS.GetAllDiagonalMain;
import com.example.TicTacToe.GAME.UTILS.GetAllDiagonalPairsWithoutMain;
import com.example.TicTacToe.GlobalException.GameExceptions.InvalidTurnException;
import com.example.TicTacToe.GlobalException.InvalidMoveException;
import com.example.TicTacToe.Models.Match;
import com.example.TicTacToe.Models.Player;
import com.example.TicTacToe.Repositories.MatchDataRepository;
import com.example.TicTacToe.Repositories.MatchRepository;
import com.example.TicTacToe.Repositories.PlayerRepositories;
import com.example.TicTacToe.Utils.JsonToString;
import com.example.TicTacToe.bot.BotEngine;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class GameSessionBotTest {
    private final MatchDataRepository matchDataRepository = mock(MatchDataRepository.class);
    private final BotEngine botEngine = new BotEngine();

    private GameSession session(GameModes mode, String startingPlayer) {
        JsonToString jsonToString = mock(JsonToString.class);
        when(jsonToString.convertJsonToString(any())).thenReturn("{}");
        return new GameSession(3, "human", "bot", "X", "O", jsonToString, matchDataRepository,
                new GameEngine(new GetAllDiagonalPairsWithoutMain(), new GetAllAntiDiagonalPairs(),
                        new GetAllDiagonalMain()),
                mock(PlayerRepositories.class), mock(MatchRepository.class), startingPlayer, mode, botEngine);
    }

    @Test
    void humanAndBotMovesAreSeparateAndHumanCannotMoveDuringBotTurn() {
        GameSession session = session(GameModes.BOTMATCH, "human");

        assertTrue(session.processMove(0, 0, "X", MoveType.NORMAL, PatternType.NONE));
        DTOGameSessionResponse humanSnapshot = GameSessionSnapshot.from(session);
        assertTrue(humanSnapshot.isBotThinking());
        assertEquals("bot", humanSnapshot.getCurrentPlayer());
        assertThrows(InvalidTurnException.class,
                () -> session.processMove(0, 1, "X", MoveType.NORMAL, PatternType.NONE));
        assertTrue(session.playBotMoveIfNeeded());

        assertEquals(1, countSymbol(humanSnapshot.getBoard(), "X"));
        assertEquals(0, countSymbol(humanSnapshot.getBoard(), "O"));
        DTOGameSessionResponse botSnapshot = GameSessionSnapshot.from(session);
        assertEquals(1, countSymbol(botSnapshot.getBoard(), "O"));
        assertEquals("human", botSnapshot.getCurrentPlayer());
        assertEquals("bot", botSnapshot.getLastMovePlayer());
        assertEquals("O", botSnapshot.getLastMove().getSymbol());
        assertFalse(botSnapshot.isBotThinking());
        verify(matchDataRepository, times(2)).save(any());
    }

    @Test
    void botStartsNewBoardWhenHumanFillsDrawnBoard() {
        GameSession session = session(GameModes.BOTMATCH, "human");
        session.setBoard(new String[][] {
                {"X", "O", "X"},
                {"X", "O", "O"},
                {"O", "X", null}
        });

        assertTrue(session.processMove(2, 2, "X", MoveType.NORMAL, PatternType.NONE));
        assertEquals(4, session.getBoardSize());
        assertEquals("bot", session.getCurrentPlayer());
        assertTrue(GameSessionSnapshot.from(session).isBotThinking());
        assertTrue(session.playBotMoveIfNeeded());
        assertEquals(1, countSymbol(session.getBoard(), "O"));
        assertEquals("human", session.getCurrentPlayer());
    }

    @Test
    void humanStartsNewBoardWhenBotFillsDrawnBoard() {
        GameSession session = session(GameModes.BOTMATCH, "bot");
        session.setBoard(new String[][] {
                {"X", "O", "X"},
                {"X", "O", "O"},
                {"O", "X", null}
        });

        assertTrue(session.playBotMoveIfNeeded());
        assertEquals(4, session.getBoardSize());
        assertEquals("human", session.getCurrentPlayer());
        assertFalse(session.isBotTurn());
    }

    @Test
    void normalMatchDoesNotTriggerBot() {
        GameSession session = session(GameModes.PVPONLINE, "human");
        assertTrue(session.processMove(0, 0, "X", MoveType.NORMAL, PatternType.NONE));
        assertFalse(session.isBotTurn());
        assertFalse(session.playBotMoveIfNeeded());
        assertEquals("bot", session.getCurrentPlayer());
    }

    @Test
    void botCompletesAndPaysForBothDoubleOverrideMoves() {
        JsonToString json = mock(JsonToString.class);
        PlayerRepositories players = mock(PlayerRepositories.class);
        Player human = new Player("human@example.com", "human", "password");
        Player bot = new Player("bot@example.com", "bot", "password");
        when(players.findPlayerByUsername("human")).thenReturn(Optional.of(human));
        when(players.findPlayerByUsername("bot")).thenReturn(Optional.of(bot));
        GameSession session = new GameSession(3, "human", "bot", "X", "O", json, matchDataRepository,
                new GameEngine(new GetAllDiagonalPairsWithoutMain(), new GetAllAntiDiagonalPairs(),
                        new GetAllDiagonalMain()), players, mock(MatchRepository.class), "bot",
                GameModes.BOTMATCH, botEngine);
        session.setMatch(new Match());
        session.setPlayer2Points(5);
        session.setTwistActive(true);
        session.setPlayer1TwistTurnsLeft(2);
        session.setPlayer2TwistTurnsLeft(2);
        session.setBoard(new String[][] {
                {"X", "O", "O"},
                {null, null, null},
                {null, null, null}
        });

        assertTrue(session.playBotMoveIfNeeded());
        assertEquals(MoveType.DOUBLE_OVERRIDE, session.getLastMove().getMoveType());
        assertTrue(session.isBotTurn());
        assertTrue(session.playBotMoveIfNeeded());
        assertEquals(MoveType.DOUBLE_OVERRIDE, session.getLastMove().getMoveType());
        assertEquals(1, session.getPlayer2Points());
        assertEquals("bot", session.getWinner());
    }

    @Test
    void twistRemainsSwappedUntilBothPlayersHaveCompletedTwoTurns() {
        GameSession session = session(GameModes.PVPONLINE, "human");
        session.setPlayer1Points(1);
        session.setPlayer2Points(1);

        assertTrue(session.processMove(0, 0, "X", MoveType.TWIST, PatternType.NONE));
        assertEquals("O", session.getBoard()[0][0]);
        assertEquals(0, session.getPlayer1Points());
        assertTrue(GameSessionSnapshot.from(session).isTwistActive());
        assertThrows(InvalidTurnException.class,
                () -> session.processMove(0, 1, "X", MoveType.TWIST, PatternType.NONE));
        assertNull(session.getBoard()[0][1]);
        assertEquals(1, session.getPlayer2Points());
        assertEquals(2, session.getPlayer2TwistTurnsLeft());
        assertTrue(session.processMove(0, 1, "X", MoveType.NORMAL, PatternType.NONE));
        assertTrue(session.processMove(1, 1, "O", MoveType.NORMAL, PatternType.NONE));
        assertEquals(0, session.getPlayer1TwistTurnsLeft());
        assertTrue(session.isTwistActive());
        assertEquals("O", session.getSymbolForPlayer("human"));
        assertEquals("X", session.getSymbolForPlayer("bot"));
        assertTrue(session.processMove(2, 1, "X", MoveType.NORMAL, PatternType.NONE));
        assertFalse(session.isTwistActive());
        assertEquals("X", session.getSymbolForPlayer("human"));
        assertEquals("O", session.getSymbolForPlayer("bot"));
    }

    @Test
    void failedTwistDoesNotChangeSessionState() {
        GameSession session = session(GameModes.PVPONLINE, "human");

        assertFalse(session.processMove(0, 0, "X", MoveType.TWIST, PatternType.NONE));
        assertFalse(session.isTwistActive());
        assertEquals(0, session.getPlayer1TwistTurnsLeft());
        assertEquals(0, session.getPlayer2TwistTurnsLeft());
        assertNull(session.getBoard()[0][0]);
        session.setPlayer1Points(1);
        assertThrows(InvalidMoveException.class,
                () -> session.processMove(-1, 0, "X", MoveType.TWIST, PatternType.NONE));
        assertFalse(session.isTwistActive());
        assertEquals(1, session.getPlayer1Points());
    }

    @Test
    void botActivatesTwistAndWinsWithHumanSymbol() {
        JsonToString json = mock(JsonToString.class);
        PlayerRepositories players = mock(PlayerRepositories.class);
        when(players.findPlayerByUsername("human"))
                .thenReturn(Optional.of(new Player("human@example.com", "human", "password")));
        when(players.findPlayerByUsername("bot"))
                .thenReturn(Optional.of(new Player("bot@example.com", "bot", "password")));
        GameSession session = new GameSession(3, "human", "bot", "X", "O", json, matchDataRepository,
                new GameEngine(new GetAllDiagonalPairsWithoutMain(), new GetAllAntiDiagonalPairs(),
                        new GetAllDiagonalMain()), players, mock(MatchRepository.class), "bot",
                GameModes.BOTMATCH, botEngine);
        session.setMatch(new Match());
        session.setPlayer2Points(1);
        session.setBoard(new String[][] {
                {"X", "X", "O"},
                {null, null, null},
                {null, null, null}
        });

        assertTrue(session.playBotMoveIfNeeded());
        assertEquals(MoveType.TWIST, session.getLastMove().getMoveType());
        assertEquals("X", session.getLastMove().getSymbol());
        assertEquals("bot", session.getWinner());
        assertTrue(session.isTwistActive());
    }

    @Test
    void botUsesItsSwappedSymbolDuringHumanTwist() {
        GameSession session = session(GameModes.BOTMATCH, "human");
        session.setPlayer1Points(1);
        session.setPlayer2Points(1);
        assertTrue(session.processMove(0, 0, "X", MoveType.TWIST, PatternType.NONE));

        assertTrue(session.playBotMoveIfNeeded());
        assertNotEquals(MoveType.TWIST, session.getLastMove().getMoveType());
        assertEquals("X", session.getLastMove().getSymbol());
        assertTrue(session.isTwistActive());
    }

    @Test
    void logsTwistLifecycleOnceWithoutPlayerNames() {
        Logger logger = (Logger) LoggerFactory.getLogger(GameSession.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        try {
            GameSession session = session(GameModes.PVPONLINE, "human");
            session.setPlayer1Points(1);
            assertTrue(session.processMove(0, 0, "X", MoveType.TWIST, PatternType.NONE));
            assertTrue(session.processMove(0, 1, "X", MoveType.NORMAL, PatternType.NONE));
            assertTrue(session.processMove(1, 1, "O", MoveType.NORMAL, PatternType.NONE));
            assertTrue(session.processMove(2, 1, "X", MoveType.NORMAL, PatternType.NONE));
            assertTrue(session.processMove(1, 0, "X", MoveType.NORMAL, PatternType.NONE));

            var events = appender.list.stream().filter(event -> event.getLevel() == Level.INFO).toList();
            assertEquals(2, events.size());
            assertTrue(events.get(0).getFormattedMessage().contains("Twist activated"));
            assertTrue(events.get(1).getFormattedMessage().contains("Twist ended"));
            assertTrue(events.stream().allMatch(event -> event.getFormattedMessage().contains(session.getSessionId())));
            assertTrue(events.stream().noneMatch(event ->
                    event.getFormattedMessage().contains("human") || event.getFormattedMessage().contains("bot")));
        } finally {
            logger.detachAppender(appender);
            appender.stop();
        }
    }

    private int countSymbol(String[][] board, String symbol) {
        int count = 0;
        for (String[] row : board) {
            for (String cell : row) {
                if (symbol.equals(cell)) {
                    count++;
                }
            }
        }
        return count;
    }
}
