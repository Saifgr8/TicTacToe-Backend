package com.example.TicTacToe.bot;

import com.example.TicTacToe.DTOs.BoardDTOs.DTOBoardMetrics;
import com.example.TicTacToe.GAME.Enums.MoveType;
import com.example.TicTacToe.GAME.Enums.PatternType;
import com.example.TicTacToe.GAME.GameEngine;
import com.example.TicTacToe.GAME.UTILS.GetAllAntiDiagonalPairs;
import com.example.TicTacToe.GAME.UTILS.GetAllDiagonalMain;
import com.example.TicTacToe.GAME.UTILS.GetAllDiagonalPairsWithoutMain;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BotEngineTest {

    private final BotEngine botEngine = new BotEngine();

    @Test
    void findBestMoveUsesPatternBombForHumanOverrideThreat() {
        String[][] board = {
                {"X", "X", "X", null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
        };

        BotMove move = botEngine.findBestMove(board, "X", "O", 2, 4, null, true);

        assertNotNull(move);
        assertEquals(MoveType.PATTERN_BOMB, move.getMoveType());
        assertEquals(PatternType.ROW, move.getPatternType());
        assertEquals(0, move.getI());
    }

    @Test
    void findBestMoveDoesNotUsePatternBombWhenBotCannotAffordIt() {
        String[][] board = {
                {"X", "X", "X", null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
        };

        BotMove move = botEngine.findBestMove(board, "X", "O", 1, 4);

        assertNotNull(move);
        assertNotEquals(MoveType.PATTERN_BOMB, move.getMoveType());
    }

    @Test
    void findBestMoveBombsLineWithTwoRemainingCellsWhenHumanHasFivePoints() {
        String[][] board = {
                {"X", "X", "O", "O"},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
        };

        BotMove move = botEngine.findBestMove(board, "X", "O", 2, 5, null, true);

        assertNotNull(move);
        assertEquals(MoveType.PATTERN_BOMB, move.getMoveType());
        assertEquals(PatternType.ROW, move.getPatternType());
        assertEquals(0, move.getI());
    }

    @Test
    void boardMetricsAndScoringHandleNullCells() {
        String[][] board = {
                {"X", null, null, null},
                {null, "O", null, null},
                {null, null, "X", null},
                {null, null, null, null}
        };

        assertDoesNotThrow(() -> botEngine.getBoardMetrics(board, "X", 0, "O"));
        BotMove move = assertDoesNotThrow(() -> botEngine.findBestMove(board, "X", "O", 0, 0));
        assertNotNull(move);
        assertEquals(MoveType.NORMAL, move.getMoveType());
    }

    @Test
    void patternBombClearsMainDiagonal() {
        String[][] board = {
                {"X", "O", null},
                {"O", "X", null},
                {null, null, "X"}
        };
        GameEngine gameEngine = new GameEngine(
                new GetAllDiagonalPairsWithoutMain(),
                new GetAllAntiDiagonalPairs(),
                new GetAllDiagonalMain()
        );

        boolean applied = gameEngine.applyMove(
                board, 0, 0, "O", MoveType.PATTERN_BOMB, PatternType.MAIN_DIAGONAL, 2
        );

        assertTrue(applied);
        assertNull(board[0][0]);
        assertNull(board[1][1]);
        assertNull(board[2][2]);
        assertEquals("O", board[0][1]);
        assertEquals("O", board[1][0]);
    }

    @Test
    void botSelectsMainDiagonalForCriticalHumanLine() {
        String[][] board = {
                {"X", null, null, null},
                {null, "X", null, null},
                {null, null, "X", null},
                {null, null, null, null}
        };

        BotMove move = botEngine.findBestMove(board, "X", "O", 2, 4, null, true);

        assertNotNull(move);
        assertEquals(MoveType.PATTERN_BOMB, move.getMoveType());
        assertEquals(PatternType.MAIN_DIAGONAL, move.getPatternType());
        assertEquals(0, move.getI());
        assertEquals(0, move.getJ());
    }

    @Test
    void offsetDiagonalsAreNotWinningThreats() {
        String[][] board = {
                {null, "X", null, null},
                {null, null, "X", null},
                {null, null, null, null},
                {null, null, null, null}
        };

        assertNull(botEngine.getBoardMetrics(board, "X", 4, "O"));
        BotMove move = botEngine.findBestMove(board, "X", "O", 2, 4, null, true);

        assertNotNull(move);
        assertEquals(MoveType.NORMAL, move.getMoveType());
    }

    @Test
    void offsetAntiDiagonalIsNotWinningThreat() {
        String[][] board = {
                {null, null, "X", null},
                {null, "X", null, null},
                {null, null, null, null},
                {null, null, null, null}
        };

        assertNull(botEngine.getBoardMetrics(board, "X", 4, "O"));
        BotMove move = botEngine.findBestMove(board, "X", "O", 2, 4);
        assertNotNull(move);
        assertEquals(MoveType.NORMAL, move.getMoveType());
    }

    @Test
    void criticalAntiDiagonalUsesTopRightBombAnchor() {
        String[][] board = {
                {null, null, null, "X"},
                {null, null, "X", null},
                {null, "X", null, null},
                {null, null, null, null}
        };

        BotMove move = botEngine.findBestMove(board, "X", "O", 2, 4, null, true);

        assertNotNull(move);
        assertEquals(MoveType.PATTERN_BOMB, move.getMoveType());
        assertEquals(PatternType.RIGHT_DIAGONAL, move.getPatternType());
        assertEquals(0, move.getI());
        assertEquals(3, move.getJ());

        GameEngine gameEngine = new GameEngine(
                new GetAllDiagonalPairsWithoutMain(), new GetAllAntiDiagonalPairs(), new GetAllDiagonalMain());
        assertTrue(gameEngine.applyMove(board, move.getI(), move.getJ(), "O",
                move.getMoveType(), move.getPatternType(), 2));
        for (int i = 0; i < board.length; i++) {
            assertNull(board[i][board.length - 1 - i]);
        }
    }

    @Test
    void actualWinningLineTakesPriorityOverOffsetDiagonal() {
        String[][] board = {
                {"X", "X", null, null, null},
                {null, null, "X", null, null},
                {null, null, null, "X", null},
                {"X", null, null, null, null},
                {"X", null, null, null, null}
        };

        DTOBoardMetrics metrics = botEngine.getBoardMetrics(board, "X", 5, "O");

        assertNotNull(metrics);
        assertEquals(PatternType.COLUMN, metrics.getCriticalPattern());
        assertEquals(0, metrics.getCriticalColumn());
        assertEquals(2, metrics.getCriticalRemainingCells());
    }

    @Test
    void findBestMovePrioritizesThreatWithOneRemainingCell() {
        String[][] board = {
                {"X", "X", null, null},
                {"X", "X", "X", null},
                {null, null, null, null},
                {null, null, null, null}
        };

        BotMove move = botEngine.findBestMove(board, "X", "O", 2, 5, null, true);

        assertNotNull(move);
        assertEquals(MoveType.PATTERN_BOMB, move.getMoveType());
        assertEquals(PatternType.ROW, move.getPatternType());
        assertEquals(1, move.getI());
    }

    @Test
    void twistWinsImmediatelyByUsingAnEmptyCell() {
        String[][] board = {
                {"X", "X", null},
                {null, null, null},
                {null, null, null}
        };
        BotMove move = botEngine.findBestMove(board, "X", "O", 1, 0);

        assertEquals(MoveType.TWIST, move.getMoveType());
        assertEquals(0, move.getI());
        assertEquals(2, move.getJ());
        assertNull(board[0][2]);
    }

    @Test
    void twistCannotWinByReplacingAnOccupiedCell() {
        String[][] board = {
                {"X", "X", "O"},
                {null, null, null},
                {null, null, null}
        };
        BotMove move = botEngine.findBestMove(board, "X", "O", 1, 0);

        assertNotNull(move);
        assertNotEquals(MoveType.TWIST, move.getMoveType());
        assertEquals("O", board[0][2]);
    }

    @Test
    void twistIsNotSelectedWithoutPointsOrWhileActive() {
        String[][] board = {
                {"X", "X", null},
                {null, "O", null},
                {null, null, null}
        };
        assertNotEquals(MoveType.TWIST, botEngine.findBestMove(board, "X", "O", 0, 0).getMoveType());
        assertNotEquals(MoveType.TWIST,
                botEngine.findBestMove(board, "X", "O", 1, 0, null, true).getMoveType());
    }

    @Test
    void botPrefersItsOwnImmediateWinAndDoesNotSpamTwist() {
        String[][] winning = {
                {"O", "O", null},
                {"X", "X", null},
                {null, null, null}
        };
        assertEquals(MoveType.NORMAL, botEngine.findBestMove(winning, "X", "O", 1, 0).getMoveType());
        String[][] early = {
                {"X", null, null},
                {null, null, null},
                {null, null, null}
        };
        assertNotEquals(MoveType.TWIST, botEngine.findBestMove(early, "X", "O", 1, 0).getMoveType());
    }

    @Test
    void twistCreatesAForcedWinAgainstEveryNormalReply() {
        String[][] board = {
                {null, "X", "X", "O"},
                {"O", null, "O", null},
                {"O", "X", null, "X"},
                {"X", null, "X", null}
        };
        BotMove twist = botEngine.findBestMove(board, "X", "O", 1, 0);
        assertEquals(MoveType.TWIST, twist.getMoveType());
        assertEquals(3, twist.getI());
        assertEquals(1, twist.getJ());
        assertNull(board[3][1]);
        board[twist.getI()][twist.getJ()] = "X";

        GameEngine gameEngine = new GameEngine(
                new GetAllDiagonalPairsWithoutMain(), new GetAllAntiDiagonalPairs(), new GetAllDiagonalMain());
        for (int row = 0; row < board.length; row++) {
            for (int col = 0; col < board.length; col++) {
                if (board[row][col] != null) {
                    continue;
                }
                board[row][col] = "O";
                BotMove followUp = botEngine.findBestMove(board, "O", "X", 0, 0, null, true);
                assertEquals(MoveType.NORMAL, followUp.getMoveType());
                assertTrue(gameEngine.applyMove(board, followUp.getI(), followUp.getJ(), "X",
                        followUp.getMoveType(), followUp.getPatternType(), 0));
                assertFalse(gameEngine.getWinningPatterns(board, followUp.getI(), followUp.getJ(), "X").isEmpty());
                board[followUp.getI()][followUp.getJ()] = null;
                board[row][col] = null;
            }
        }
    }

    @Test
    void botDoesNotAssumeNormalRepliesWhenHumanCanAffordSpecialMoves() {
        String[][] board = {
                {null, "X", "X", "O"},
                {"O", null, "O", null},
                {"O", "X", null, "X"},
                {"X", null, "X", null}
        };
        assertNotEquals(MoveType.TWIST, botEngine.findBestMove(board, "X", "O", 1, 2).getMoveType());
    }
}
