package com.example.TicTacToe.GAME;


import com.example.TicTacToe.GAME.Enums.MoveType;
import com.example.TicTacToe.GAME.Enums.PatternType;
import com.example.TicTacToe.GAME.UTILS.GetAllAntiDiagonalPairs;
import com.example.TicTacToe.GAME.UTILS.GetAllDiagonalMain;
import com.example.TicTacToe.GAME.UTILS.GetAllDiagonalPairsWithoutMain;
import com.example.TicTacToe.GlobalException.InvalidMoveException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class GameEngine {
    private final GetAllDiagonalPairsWithoutMain diagonalPairs;
    private final GetAllAntiDiagonalPairs antiDiagonalPairs;
    private final GetAllDiagonalMain diagonalMain;


    public GameEngine(GetAllDiagonalPairsWithoutMain diagonalPairs, GetAllAntiDiagonalPairs antiDiagonalPairs
            , GetAllDiagonalMain diagonalMain) {
        this.diagonalPairs = diagonalPairs;
        this.antiDiagonalPairs = antiDiagonalPairs;
        this.diagonalMain = diagonalMain;

    }

    public boolean isValidMove(String[][] board, int i, int j, String symbol, MoveType moveType) {
        int size = board.length;
        // BOUNDARY CHECK
        if (i < 0 || j < 0 || i >= size || j >= size) throw new InvalidMoveException("Current move is out of bonds");

        String cell = board[i][j];

        //MoveType VALIDATION
        //NORMAL MOVE -> ONLY ON EMPTY CELL
        if (moveType == MoveType.NORMAL) {
            if (cell != null && !cell.trim().isEmpty()) {
                throw new InvalidMoveException("Cell already occupied, Please select other cell");
            }
            return true;
        }
        //  OVERRIDE, EMPTY_CELL, DOUBLE_OVERRIDE -> OPPONENT OCCUPIED CELL ONLY
        else if (moveType == MoveType.OVERRIDE ||
                moveType == MoveType.EMPTY_CELL ||
                moveType == MoveType.DOUBLE_OVERRIDE) {

            if (cell == null || cell.isEmpty())
                throw new InvalidMoveException("Cell is empty, please select opponent's cell");
            if (cell.equals(symbol)) throw new InvalidMoveException("Please select opponent's cell");
            return true;
        }
        //PATTERN -> WILL DO LATER
        else if (moveType == MoveType.PATTERN_BOMB || moveType == MoveType.TWIST) {
            return true;
        }
        return false;
    }

    public boolean applyMove(String[][] board, int i, int j, String symbol, MoveType moveType, PatternType patternType
            , int currentPlayerPoints) {
        //VALIDATE MOVE
        if (!isValidMove(board, i, j, symbol, moveType)) return false;

        //NORMAL MOVE WRITE
        if (moveType == MoveType.NORMAL
                || (moveType == MoveType.TWIST && currentPlayerPoints >= 1)) {
            board[i][j] = symbol;
            return true;
        }
        //OVERRIDE MOVE WRITE
        else if ((moveType == MoveType.OVERRIDE && currentPlayerPoints >= 4) ||
                (moveType == MoveType.DOUBLE_OVERRIDE && currentPlayerPoints >= 5)) {

            board[i][j] = symbol;
            return true;
        } else if (moveType == MoveType.EMPTY_CELL && currentPlayerPoints >= 2) {
            board[i][j] = null;
            return true;
        }
        //PATTERN ->
        else if (moveType == MoveType.PATTERN_BOMB && currentPlayerPoints >= 2) {
            switch (patternType) {
                case ROW -> {
                    for (int col = 0; col < board.length; col++) {
                        board[i][col] = null;
                    }
                }
                case COLUMN -> {
                    for (int row = 0; row < board.length; row++) {
                        board[row][j] = null;
                    }
                }
                case LEFT_DIAGONAL -> {
                    int row = i;
                    int col = j;
                    while (row > 0 && col > 0) {
                        row--;
                        col--;
                    }
                    while (row < board.length && col < board.length) {
                        board[row][col] = null;
                        row++;
                        col++;
                    }
                }
                case RIGHT_DIAGONAL -> {
                    int row = i;
                    int col = j;
                    while (row > 0 && col < board.length - 1) {
                        row--;
                        col++;
                    }
                    while (row < board.length && col >= 0) {
                        board[row][col] = null;
                        row++;
                        col--;
                    }
                }
                case MAIN_DIAGONAL -> {
                    for (int index = 0; index < board.length; index++) {
                        board[index][index] = null;
                    }
                }
                case NONE -> throw new InvalidMoveException("Pattern type is required for a pattern bomb");
            }
            return true; // ✅ Critical to mark move as valid]
        }

        return false;
    }

    public List<String> getWinningPatterns(String[][] board, int i, int j, String symbol) {
        int size = board.length;
        List<String> winningPatterns = new ArrayList<>();

        // ==== CHECK ROW ====
        boolean rowFlag = true;
        for (int col = 0; col < size; col++) {
            if (!symbol.equals(board[i][col])) {
                rowFlag = false;
                break;
            }
        }
        if (rowFlag) {
            // Add all cells of the row
            winningPatterns.add("ROW-" + i);
        }

        // ==== CHECK COLUMN ====
        boolean colFlag = true;
        for (int row = 0; row < size; row++) {
            if (!symbol.equals(board[row][j])) {
                colFlag = false;
                break;
            }
        }
        if (colFlag) {
            winningPatterns.add("COL-" + j);
        }

        // ==== MAIN DIAGONAL ====
        if (i == j) {
            boolean mainFlag = true;
            for (int val = 0; val < size; val++) {
                if (!symbol.equals(board[val][val])) {
                    mainFlag = false;
                    break;
                }
            }
            if (mainFlag) {
                winningPatterns.add("MD-0");
            }
        }

        // ==== ANTI DIAGONAL ====
        if (i + j == size - 1) {
            boolean antiFlag = true;
            for (int val = 0; val < size; val++) {
                if (!symbol.equals(board[val][size - val - 1])) {
                    antiFlag = false;
                    break;
                }
            }
            if (antiFlag) {
                winningPatterns.add("AD-0");
            }
        }

        return winningPatterns;
    }

    public boolean isDraw(String[][] board) {
        for (String[] row : board) {
            for (String cell : row) {
                if (cell == null || cell.isEmpty()) return false;
            }
        }
        return true;
    }

    public List<String> countThreePattern(String[][] board, String symbol) {
        int size = board.length;
        List<String> threePatternList = new ArrayList<>();

        //ROWS
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size - 2; j++) {
                if (symbol.equals(board[i][j]) &&
                        symbol.equals(board[i][j + 1]) &&
                        symbol.equals(board[i][j + 2])) {
                    threePatternList.add("H-" + i + "-" + j + "-" + (j + 1) + "-" + (j + 2));
                }
            }
        }

        //COLS
        for (int j = 0; j < size; j++) {
            for (int i = 0; i < size - 2; i++) {
                if (symbol.equals(board[i][j]) &&
                        symbol.equals(board[i + 1][j]) &&
                        symbol.equals(board[i + 2][j])) {
                    threePatternList.add("V-" + j + "-" + i + "-" + (i + 1) + "-" + (i + 2));
                }
            }
        }
        //DIAGONAL -> MAIN
        List<String> mainDiagonal = diagonalMain.getAllDiagonalMainPairs(board);
        for (String item : mainDiagonal) {
            String[] parts = item.split("-");
            int index = Integer.parseInt(parts[0]);
            int one = Integer.parseInt(parts[1]);
            int two = Integer.parseInt(parts[2]);
            int three = Integer.parseInt(parts[3]);
            if (symbol.equals(board[one][one]) && symbol.equals(board[two][two])
                    && symbol.equals(board[three][three])) {
                threePatternList.add("MD-" + index + "-" + one + "-" + two + "-" + three);
            }
        }
        //DIAGONAL -> REST
        List<String> diagonalPair = diagonalPairs.getAllDiagonalPairs(board);
        for (String item : diagonalPair) {
            String[] parts = item.split("-");
            int index = Integer.parseInt(parts[0]);
            int one = Integer.parseInt(parts[1]);
            int two = Integer.parseInt(parts[2]);
            int three = Integer.parseInt(parts[3]);
            if (symbol.equals(board[index][one]) && symbol.equals(board[index + 1][two])
                    && symbol.equals(board[index + 2][three])) {
                threePatternList.add("D-" + index + "-" + one + "-" + two + "-" + three);
            }
        }

        //ANTI DIAGONAL -> All -> //0-432 1->321 2->210
        List<String> antiDiagPairs = antiDiagonalPairs.getAllAntiDiagPatterns(board);
        for (String item : antiDiagPairs) {
            String[] parts = item.split("-");
            int index = Integer.parseInt(parts[0]);
            int three = Integer.parseInt(parts[1]);
            int two = Integer.parseInt(parts[2]);
            int one = Integer.parseInt(parts[3]);
            if (symbol.equals(board[index][three]) && symbol.equals(board[index + 1][two])
                    && symbol.equals(board[index + 2][one])) {
                threePatternList.add("A-" + index + "-" + three + "-" + two + "-" + one);
            }
        }

        return threePatternList;
    }
}
