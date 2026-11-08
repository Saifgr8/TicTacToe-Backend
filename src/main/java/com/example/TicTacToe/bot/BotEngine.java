package com.example.TicTacToe.bot;

import com.example.TicTacToe.DTOs.BoardDTOs.DTOBoardMetrics;
import com.example.TicTacToe.DTOs.GameDTOs.DTOGameMove;
import com.example.TicTacToe.GAME.Enums.MoveType;
import com.example.TicTacToe.GAME.Enums.PatternType;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class BotEngine {

    private record BoardPosition(int row, int column) {
    }

    /**
     * Searches full rows, columns, and the two full-board diagonals for a
     * potential winning move that requires overriding one or more human cells.
     *
     * @param board the current square game board
     * @param botSymbol the symbol used by the bot
     * @param botPoints the bot's available points
     * @param humanPoints the human player's available points (currently unused)
     * @param humanSymbol the symbol used by the human player
     * @param prevMoveType the previous move, used to determine whether a
     *                     double override is active
     * @return an override move when a qualifying line is found, or {@code null}
     */
    public BotMove findWinningMoveWithPoints(String[][] board, String botSymbol,
                                             int botPoints, int humanPoints, String humanSymbol,
                                             DTOGameMove prevMoveType) {
        if (board == null || board.length == 0 || botSymbol == null || humanSymbol == null) {
            return null;
        }

        int size = board.length;
        boolean doubleOverrideActive = prevMoveType != null
                && MoveType.DOUBLE_OVERRIDE == prevMoveType.getMoveType();

        for (int row = 0; row < size; row++) {
            List<BoardPosition> line = new ArrayList<>();
            for (int column = 0; column < size; column++) {
                line.add(new BoardPosition(row, column));
            }
            BotMove move = findOverrideMoveInLine(board, line, botSymbol, humanSymbol,
                    botPoints, doubleOverrideActive);
            if (move != null) {
                return move;
            }
        }

        for (int column = 0; column < size; column++) {
            List<BoardPosition> line = new ArrayList<>();
            for (int row = 0; row < size; row++) {
                line.add(new BoardPosition(row, column));
            }
            BotMove move = findOverrideMoveInLine(board, line, botSymbol, humanSymbol,
                    botPoints, doubleOverrideActive);
            if (move != null) {
                return move;
            }
        }

        List<BoardPosition> mainDiagonal = new ArrayList<>();
        List<BoardPosition> antiDiagonal = new ArrayList<>();
        for (int index = 0; index < size; index++) {
            mainDiagonal.add(new BoardPosition(index, index));
            antiDiagonal.add(new BoardPosition(index, size - index - 1));
        }

        BotMove move = findOverrideMoveInLine(board, mainDiagonal, botSymbol, humanSymbol,
                botPoints, doubleOverrideActive);
        return move != null ? move : findOverrideMoveInLine(board, antiDiagonal,
                botSymbol, humanSymbol, botPoints, doubleOverrideActive);
    }

    /**
     * Finds the most urgent full row, column, main diagonal, or anti-diagonal
     * with one or two cells not occupied by the human, when the human has
     * enough points for an override or double override.
     *
     * @param board the current square game board
     * @param humanSymbol the human player's symbol
     * @param humanPoints the human player's available points
     * @param botSymbol the bot's symbol; retained for compatibility and unused
     * @return metrics including the threatened pattern and a valid anchor
     */
    public DTOBoardMetrics getBoardMetrics(String[][] board, String humanSymbol, int humanPoints, String botSymbol) {
        if (board == null || board.length == 0 || humanSymbol == null) {
            return null;
        }

        int n = board.length;
        DTOBoardMetrics bestThreat = null;
        int fewestRemainingCells = Integer.MAX_VALUE;
        for (int row = 0; row < n; row++) {
            int humanCount = 0;
            for (int col = 0; col < n; col++) {
                if (humanSymbol.equals(board[row][col])) {
                    humanCount++;
                }
            }
            int remainingCells = n - humanCount;
            if (isCriticalHumanLine(humanCount, n, humanPoints)
                    && remainingCells < fewestRemainingCells) {
                bestThreat = createCriticalMetrics(PatternType.ROW, row, 0, 0, remainingCells);
                fewestRemainingCells = remainingCells;
            }
        }

        for (int col = 0; col < n; col++) {
            int humanCount = 0;
            for (int row = 0; row < n; row++) {
                if (humanSymbol.equals(board[row][col])) {
                    humanCount++;
                }
            }
            int remainingCells = n - humanCount;
            if (isCriticalHumanLine(humanCount, n, humanPoints)
                    && remainingCells < fewestRemainingCells) {
                bestThreat = createCriticalMetrics(PatternType.COLUMN, 0, col, 0, remainingCells);
                fewestRemainingCells = remainingCells;
            }
        }

        DTOBoardMetrics mainDiagonal = inspectDiagonal(
                board, humanSymbol, humanPoints, 0, 0, 1, 1, PatternType.MAIN_DIAGONAL, 0);
        if (mainDiagonal != null && mainDiagonal.getCriticalRemainingCells() < fewestRemainingCells) {
            bestThreat = mainDiagonal;
            fewestRemainingCells = mainDiagonal.getCriticalRemainingCells();
        }

        DTOBoardMetrics antiDiagonal = inspectDiagonal(
                board, humanSymbol, humanPoints, 0, n - 1, 1, -1, PatternType.RIGHT_DIAGONAL, 0);
        if (antiDiagonal != null && antiDiagonal.getCriticalRemainingCells() < fewestRemainingCells) {
            bestThreat = antiDiagonal;
        }

        return bestThreat;
    }

    /**
     * Counts human marks along one diagonal and returns metrics if that line
     * meets the critical-point threshold.
     *
     * @param board the current square board
     * @param humanSymbol the human player's symbol
     * @param humanPoints the human player's available points
     * @param startRow diagonal anchor row
     * @param startColumn diagonal anchor column
     * @param rowStep row direction for traversing the diagonal
     * @param columnStep column direction for traversing the diagonal
     * @param patternType orientation of the diagonal
     * @param diagonalIndex index for the corresponding legacy metric
     * @return critical-line metrics, or {@code null} if this diagonal is not critical
     */
    private DTOBoardMetrics inspectDiagonal(String[][] board, String humanSymbol, int humanPoints,
                                            int startRow, int startColumn, int rowStep, int columnStep,
                                            PatternType patternType, int diagonalIndex) {
        int humanCount = 0;
        int lineLength = 0;
        int row = startRow;
        int column = startColumn;
        while (row >= 0 && row < board.length && column >= 0 && column < board.length) {
            if (humanSymbol.equals(board[row][column])) {
                humanCount++;
            }
            lineLength++;
            row += rowStep;
            column += columnStep;
        }

        if (lineLength < 3 || !isCriticalHumanLine(humanCount, lineLength, humanPoints)) {
            return null;
        }
        return createCriticalMetrics(patternType, startRow, startColumn, diagonalIndex,
                lineLength - humanCount);
    }

    /**
     * Creates metrics for one detected critical line while retaining the
     * corresponding legacy index/comment fields.
     *
     * @param patternType orientation of the critical line
     * @param row valid anchor row for the line
     * @param column valid anchor column for the line
     * @param diagonalIndex index used by the legacy diagonal metric field
     * @param remainingCells number of cells not occupied by the human
     * @return populated critical-line metrics
     */
    private DTOBoardMetrics createCriticalMetrics(PatternType patternType, int row, int column,
                                                  int diagonalIndex, int remainingCells) {
        DTOBoardMetrics metrics = new DTOBoardMetrics(-1, -1, -1, -1, "", "", "", "");
        metrics.setCriticalPattern(patternType);
        metrics.setCriticalRow(row);
        metrics.setCriticalColumn(column);
        metrics.setCriticalRemainingCells(remainingCells);

        switch (patternType) {
            case ROW -> {
                metrics.setRowIndex(row);
                metrics.setRowComment("CRITICAL");
            }
            case COLUMN -> {
                metrics.setColIndex(column);
                metrics.setColComment("CRITICAL");
            }
            case LEFT_DIAGONAL, MAIN_DIAGONAL -> {
                metrics.setLeftDiagonalIndex(diagonalIndex);
                metrics.setLeftDiagonalComment("CRITICAL");
            }
            case RIGHT_DIAGONAL -> {
                metrics.setRightDiagonalIndex(diagonalIndex);
                metrics.setRightDiagonalComment("CRITICAL");
            }
            default -> throw new IllegalArgumentException("Unsupported critical pattern: " + patternType);
        }
        return metrics;
    }

    /**
     * Selects a move using points-aware defense, immediate win/block checks,
     * scoring opportunities, and a setup move before a random fallback.
     *
     * @param board the current square game board
     * @param humanSymbol the human player's symbol
     * @param botSymbol the bot's symbol
     * @param botPoints the bot's available points
     * @param humanPoints the human player's available points
     * @return the selected move, or {@code null} if the board has no empty cell
     */
    public BotMove findBestMove(String[][] board, String humanSymbol, String botSymbol,
                                int botPoints, int humanPoints) {
        return findBestMove(board, humanSymbol, botSymbol, botPoints, humanPoints, null);
    }

    /**
     * Selects a move while accounting for an active double override.
     *
     * @param board the current square game board
     * @param humanSymbol the human player's symbol
     * @param botSymbol the bot's symbol
     * @param botPoints the bot's available points
     * @param humanPoints the human player's available points
     * @param previousMove the bot's previous move if its double override is active
     * @return the selected move, or {@code null} if none is available
     */
    public BotMove findBestMove(String[][] board, String humanSymbol, String botSymbol,
                                int botPoints, int humanPoints, DTOGameMove previousMove) {
        return findBestMove(board, humanSymbol, botSymbol, botPoints, humanPoints, previousMove, false);
    }

    /**
     * Selects a move using the players' effective symbols and current twist state.
     *
     * @param board the current square game board
     * @param humanSymbol the human's effective symbol
     * @param botSymbol the bot's effective symbol
     * @param botPoints the bot's available points
     * @param humanPoints the human's available points
     * @param previousMove the bot's previous move if its double override is active
     * @param twistActive whether a twist is already active
     * @return the selected move, or {@code null} if none is available
     */
    public BotMove findBestMove(String[][] board, String humanSymbol, String botSymbol,
                                int botPoints, int humanPoints, DTOGameMove previousMove,
                                boolean twistActive) {
        if (board == null || board.length == 0) {
            return null;
        }

        if (previousMove != null && previousMove.getMoveType() == MoveType.DOUBLE_OVERRIDE) {
            return findWinningMoveWithPoints(board, botSymbol, botPoints, humanPoints, humanSymbol, previousMove);
        }

        BotMove botWinningMove = findImmediateCompletion(board, botSymbol);
        if (botWinningMove != null) {
            return botWinningMove;
        }

        if (!twistActive && botPoints >= 1) {
            BotMove twistWin = findTwistWin(board, humanSymbol, botSymbol);
            if (twistWin != null) {
                return twistWin;
            }
        }

        BotMove botWinWithPoints = findWinningMoveWithPoints(board, botSymbol, botPoints, humanPoints, humanSymbol, previousMove);
        if (botWinWithPoints != null) {
            return botWinWithPoints;
        }

        if (!twistActive && botPoints >= 1 && humanPoints < 2) {
            BotMove twistDefense = findTwistDefense(board, humanSymbol, botSymbol, botPoints, humanPoints);
            if (twistDefense != null) {
                return twistDefense;
            }
        }

        DTOBoardMetrics metrics = getBoardMetrics(board, humanSymbol, humanPoints, botSymbol);
        if (botPoints >= 2 && metrics != null) {
            return createPatternBombMove(metrics);
        }

        BotMove humanWinBlock = findImmediateCompletion(board, humanSymbol);
        if (humanWinBlock != null) {
            return humanWinBlock;
        }

        BotMove scoringMove = findScoringMove(board, botSymbol);
        if (scoringMove != null) {
            return scoringMove;
        }

        BotMove blockingMove = findScoringMove(board, humanSymbol);
        if (blockingMove != null) {
            return blockingMove;
        }

        BotMove setupMove = findScoringSetupMove(board, botSymbol);
        return setupMove != null ? setupMove : randomSafeMove(board);
    }

    /**
     * Finds a twist placement that immediately completes the human's current line.
     *
     * @param board the current board
     * @param humanSymbol the symbol the bot would use after twisting
     * @param botSymbol the bot's current symbol
     * @return an immediately winning twist, or {@code null}
     */
    private BotMove findTwistWin(String[][] board, String humanSymbol, String botSymbol) {
        BotMove completion = findImmediateCompletion(board, humanSymbol);
        if (completion == null) {
            completion = findWinningMoveWithPoints(board, humanSymbol, 4, 0, botSymbol, null);
        }
        return completion == null ? null
                : new BotMove(completion.getI(), PatternType.NONE, MoveType.TWIST, completion.getJ());
    }

    /**
     * Uses a twist against a near-winning line only if every normal reply still
     * leaves the bot a winning move. This conservative search is limited to
     * opponents unable to afford another special move.
     *
     * @param board the current board
     * @param humanSymbol the human's current symbol
     * @param botSymbol the bot's current symbol
     * @param botPoints the bot's available points before twisting
     * @param humanPoints the human's available points
     * @return a defensive twist with a forced follow-up win, or {@code null}
     */
    private BotMove findTwistDefense(String[][] board, String humanSymbol, String botSymbol,
                                     int botPoints, int humanPoints) {
        if (humanSymbol == null || botSymbol == null || !hasNearWinningThreat(board, humanSymbol)) {
            return null;
        }
        String[][] simulated = copyBoard(board);
        for (int row = 0; row < board.length; row++) {
            for (int column = 0; column < board.length; column++) {
                if (humanSymbol.equals(board[row][column])) {
                    continue;
                }
                simulated[row][column] = humanSymbol;
                boolean forcedWin = hasForcedTwistFollowUp(
                        simulated, humanSymbol, botSymbol, botPoints - 1, humanPoints);
                simulated[row][column] = board[row][column];
                if (forcedWin) {
                    return new BotMove(row, PatternType.NONE, MoveType.TWIST, column);
                }
            }
        }
        return null;
    }

    private boolean hasNearWinningThreat(String[][] board, String symbol) {
        if (board.length < 4) {
            return false;
        }
        String[][] simulated = copyBoard(board);
        for (int row = 0; row < board.length; row++) {
            for (int column = 0; column < board.length; column++) {
                if (!isCellEmpty(board[row][column])) {
                    continue;
                }
                simulated[row][column] = symbol;
                boolean threatens = findImmediateCompletion(simulated, symbol) != null;
                simulated[row][column] = board[row][column];
                if (threatens) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean hasForcedTwistFollowUp(String[][] board, String botSymbol, String humanSymbol,
                                           int botPoints, int humanPoints) {
        if (botPoints < 4 && findImmediateCompletion(board, botSymbol) == null) {
            return false;
        }
        boolean hasReply = false;
        for (int row = 0; row < board.length; row++) {
            for (int column = 0; column < board.length; column++) {
                if (!isCellEmpty(board[row][column])) {
                    continue;
                }
                hasReply = true;
                String original = board[row][column];
                board[row][column] = humanSymbol;
                boolean humanWins = completesLine(board, row, column, humanSymbol);
                boolean botCanWin = hasEmptyCell(board)
                        && (findImmediateCompletion(board, botSymbol) != null
                        || findWinningMoveWithPoints(board, botSymbol, botPoints,
                        humanPoints, humanSymbol, null) != null);
                board[row][column] = original;
                if (humanWins || !botCanWin) {
                    return false;
                }
            }
        }
        return hasReply;
    }

    private boolean hasEmptyCell(String[][] board) {
        for (String[] row : board) {
            for (String cell : row) {
                if (isCellEmpty(cell)) {
                    return true;
                }
            }
        }
        return false;
    }

    private String[][] copyBoard(String[][] board) {
        String[][] copy = new String[board.length][];
        for (int row = 0; row < board.length; row++) {
            copy[row] = board[row].clone();
        }
        return copy;
    }

    private boolean completesLine(String[][] board, int row, int column, String symbol) {
        boolean rowComplete = true;
        boolean columnComplete = true;
        boolean mainComplete = row == column;
        boolean antiComplete = row + column == board.length - 1;
        for (int index = 0; index < board.length; index++) {
            rowComplete &= symbol.equals(board[row][index]);
            columnComplete &= symbol.equals(board[index][column]);
            mainComplete &= symbol.equals(board[index][index]);
            antiComplete &= symbol.equals(board[index][board.length - 1 - index]);
        }
        return rowComplete || columnComplete || mainComplete || antiComplete;
    }

    /**
     * Selects one empty cell uniformly at random as a normal move.
     *
     * @param board the current square game board
     * @return a normal move, or {@code null} if there are no empty cells
     */
    public BotMove randomSafeMove(String[][] board) {
        if (board == null || board.length == 0) {
            return null;
        }

        List<BoardPosition> emptyCells = new ArrayList<>();
        for (int row = 0; row < board.length; row++) {
            for (int column = 0; column < board[row].length; column++) {
                if (isCellEmpty(board[row][column])) {
                    emptyCells.add(new BoardPosition(row, column));
                }
            }
        }

        if (emptyCells.isEmpty()) {
            return null;
        }

        BoardPosition selected = emptyCells.get(ThreadLocalRandom.current().nextInt(emptyCells.size()));
        return new BotMove(selected.row(), PatternType.NONE, MoveType.NORMAL, selected.column());
    }

    /**
     * Prefers an empty cell that creates the most distinct next-turn scoring
     * opportunities in unblocked three-cell patterns.
     */
    private BotMove findScoringSetupMove(String[][] board, String botSymbol) {
        if (botSymbol == null) {
            return null;
        }

        int size = board.length;
        int[][] directions = {{0, 1}, {1, 0}, {1, 1}, {1, -1}};
        BotMove bestMove = null;
        int bestScore = 0;
        for (int row = 0; row < size; row++) {
            for (int column = 0; column < board[row].length; column++) {
                if (!isCellEmpty(board[row][column])) {
                    continue;
                }

                boolean[][] completions = new boolean[size][size];
                int score = 0;
                for (int[] direction : directions) {
                    int rowStep = direction[0];
                    int columnStep = direction[1];
                    // Match the diagonal patterns recognized by findScoringMove.
                    if (rowStep == 1 && columnStep == 1 && row != column
                            || rowStep == 1 && columnStep == -1 && row + column != size - 1) {
                        continue;
                    }
                    for (int offset = 0; offset < 3; offset++) {
                        int startRow = row - offset * rowStep;
                        int startColumn = column - offset * columnStep;
                        int botCount = 0;
                        BoardPosition completion = null;
                        boolean blocked = false;
                        for (int index = 0; index < 3; index++) {
                            int r = startRow + index * rowStep;
                            int c = startColumn + index * columnStep;
                            if (!inBounds(r, size) || !inBounds(c, size)) {
                                blocked = true;
                                break;
                            }
                            if (r == row && c == column) {
                                continue;
                            }
                            if (botSymbol.equals(board[r][c])) {
                                botCount++;
                            } else if (isCellEmpty(board[r][c])) {
                                completion = new BoardPosition(r, c);
                            } else {
                                blocked = true;
                                break;
                            }
                        }
                        if (!blocked && botCount == 1 && completion != null
                                && !completions[completion.row()][completion.column()]) {
                            completions[completion.row()][completion.column()] = true;
                            score++;
                        }
                    }
                }
                if (score > bestScore) {
                    bestScore = score;
                    bestMove = new BotMove(row, PatternType.NONE, MoveType.NORMAL, column);
                }
            }
        }
        return bestMove;
    }

    /**
     * Applies the human's override thresholds to a line's number of unclaimed
     * cells.
     *
     * @param humanCount number of cells containing the human symbol
     * @param lineLength number of cells in the line
     * @param humanPoints human player's current points
     * @return whether the human can fill the line using an override move
     */
    private boolean isCriticalHumanLine(int humanCount, int lineLength, int humanPoints) {
        return humanCount == lineLength - 1 && humanPoints >= 4
                || humanCount == lineLength - 2 && humanPoints >= 5;
    }

    /**
     * Converts detected critical-line metrics into the corresponding
     * pattern-bomb move with an anchor accepted by the game engine.
     *
     * @param metrics detected critical-line data
     * @return the pattern-bomb move, or {@code null} if metrics are absent
     */
    private BotMove createPatternBombMove(DTOBoardMetrics metrics) {
        if (metrics == null) {
            return null;
        }
        return new BotMove(
                metrics.getCriticalRow(),
                metrics.getCriticalPattern(),
                MoveType.PATTERN_BOMB,
                metrics.getCriticalColumn()
        );
    }

    /**
     * Finds a single empty cell that completes a full row, column, or
     * full-board diagonal for the requested symbol.
     *
     * @param board the current square board
     * @param symbol symbol to complete the line
     * @return a normal move that completes a line, or {@code null}
     */
    private BotMove findImmediateCompletion(String[][] board, String symbol) {
        if (board == null || board.length == 0 || symbol == null) {
            return null;
        }

        int size = board.length;
        for (int row = 0; row < size; row++) {
            List<BoardPosition> line = new ArrayList<>();
            for (int column = 0; column < size; column++) {
                line.add(new BoardPosition(row, column));
            }
            BotMove move = findCompletionInLine(board, symbol, line);
            if (move != null) {
                return move;
            }
        }

        for (int column = 0; column < size; column++) {
            List<BoardPosition> line = new ArrayList<>();
            for (int row = 0; row < size; row++) {
                line.add(new BoardPosition(row, column));
            }
            BotMove move = findCompletionInLine(board, symbol, line);
            if (move != null) {
                return move;
            }
        }

        List<BoardPosition> mainDiagonal = new ArrayList<>();
        List<BoardPosition> antiDiagonal = new ArrayList<>();
        for (int index = 0; index < size; index++) {
            mainDiagonal.add(new BoardPosition(index, index));
            antiDiagonal.add(new BoardPosition(index, size - index - 1));
        }

        BotMove move = findCompletionInLine(board, symbol, mainDiagonal);
        return move != null ? move : findCompletionInLine(board, symbol, antiDiagonal);
    }

    /**
     * Checks one candidate winning line for exactly one empty cell and no
     * opposing marks.
     *
     * @param board the current board
     * @param symbol symbol to complete the line
     * @param line board coordinates making up the line
     * @return the completing normal move, or {@code null}
     */
    private BotMove findCompletionInLine(String[][] board, String symbol, List<BoardPosition> line) {
        int symbolCount = 0;
        BoardPosition emptyCell = null;
        for (BoardPosition position : line) {
            String cell = board[position.row()][position.column()];
            if (symbol.equals(cell)) {
                symbolCount++;
            } else if (isCellEmpty(cell)) {
                emptyCell = position;
            } else {
                return null;
            }
        }

        if (symbolCount == line.size() - 1 && emptyCell != null) {
            return new BotMove(emptyCell.row(), PatternType.NONE, MoveType.NORMAL, emptyCell.column());
        }
        return null;
    }

    /**
     * Finds a winning override or double-override opportunity in a fully
     * occupied line.
     *
     * @param board the current board
     * @param line board coordinates making up the line
     * @param botSymbol the bot's symbol
     * @param humanSymbol the human player's symbol
     * @param botPoints the bot's available points
     * @param doubleOverrideActive whether the previous move activated this ability
     * @return an override move that completes the line, or {@code null}
     */
    private BotMove findOverrideMoveInLine(String[][] board, List<BoardPosition> line,
                                           String botSymbol, String humanSymbol, int botPoints,
                                           boolean doubleOverrideActive) {
        int botCount = 0;
        int humanCount = 0;
        BoardPosition firstHumanCell = null;
        for (BoardPosition position : line) {
            String cell = board[position.row()][position.column()];
            if (botSymbol.equals(cell)) {
                botCount++;
            } else if (humanSymbol.equals(cell)) {
                humanCount++;
                if (firstHumanCell == null) {
                    firstHumanCell = position;
                }
            } else {
                return null;
            }
        }

        MoveType moveType = null;
        if (botCount == line.size() - 1 && humanCount == 1 && botPoints >= 4) {
            moveType = doubleOverrideActive && botPoints >= 5
                    ? MoveType.DOUBLE_OVERRIDE
                    : MoveType.OVERRIDE;
        } else if (botCount == line.size() - 2 && humanCount == 2 && botPoints >= 5) {
            moveType = MoveType.DOUBLE_OVERRIDE;
        }

        if (moveType == null || firstHumanCell == null) {
            return null;
        }
        return new BotMove(firstHumanCell.row(), PatternType.NONE, moveType, firstHumanCell.column());
    }

    /**
     * Finds an empty cell whose placement forms a contiguous three-cell
     * pattern for the requested symbol.
     *
     * @param board the current board
     * @param symbol symbol for which to find a scoring placement
     * @return the normal scoring move, or {@code null}
     */
    private BotMove findScoringMove(String[][] board, String symbol) {
        if (board == null || board.length == 0 || symbol == null) {
            return null;
        }

        for (int row = 0; row < board.length; row++) {
            for (int column = 0; column < board[row].length; column++) {
                if (isCellEmpty(board[row][column])
                        && (formsThree(row, column, board, symbol, "ROW")
                        || formsThree(row, column, board, symbol, "COLUMN")
                        || formsThree(row, column, board, symbol, "MAIN_DIAGONAL")
                        || formsThree(row, column, board, symbol, "ANTI_DIAGONAL"))) {
                    return new BotMove(row, PatternType.NONE, MoveType.NORMAL, column);
                }
            }
        }
        return null;
    }

    /**
     * Determines whether a board cell is empty.
     *
     * @param cell the cell value to inspect
     * @return {@code true} for {@code null} or an empty string
     */
    private boolean isCellEmpty(String cell) {
        return cell == null || cell.isEmpty();
    }

    /**
     * Scans three-cell windows along down-right diagonals starting at the
     * supplied position for two human symbols and one empty cell.
     *
     * @param row starting row
     * @param col starting column
     * @param board the current square game board
     * @param humanSymbol the human player's symbol
     * @return {@code true} if such a three-cell window is found
     */
    private boolean checkLeftDiagonalFrom(
            int row,
            int col,
            String[][] board,
            String humanSymbol
    ) {
        int n = board.length;

        while (row + 2 < n && col + 2 < n) {

            int humanCnt = 0;
            int emptyCnt = 0;

            String first = board[row][col];
            String second = board[row + 1][col + 1];
            String third = board[row + 2][col + 2];

            String[] window = {first, second, third};

            for (String item : window) {
                if (humanSymbol.equals(item)) {
                    humanCnt++;
                } else if (isCellEmpty(item)) {
                    emptyCnt++;
                }
            }

            if (humanCnt == 2 && emptyCnt == 1) {
                return true;
            }

            row++;
            col++;
        }

        return false;
    }

    /**
     * Scans three-cell windows along down-left diagonals starting at the
     * supplied position for two human symbols and one empty cell.
     *
     * @param row starting row
     * @param col starting column
     * @param board the current square game board
     * @param humanSymbol the human player's symbol
     * @return {@code true} if such a three-cell window is found
     */
    private boolean checkRightDiagonalFrom(
            int row,
            int col,
            String[][] board,
            String humanSymbol
    ) {
        int n = board.length;

        while (row + 2 < n && col - 2 >= 0) {

            int humanCnt = 0;
            int emptyCnt = 0;

            String first = board[row][col];
            String second = board[row + 1][col - 1];
            String third = board[row + 2][col - 2];

            String[] window = {first, second, third};

            for (String item : window) {
                if (humanSymbol.equals(item)) {
                    humanCnt++;
                } else if (isCellEmpty(item)) {
                    emptyCnt++;
                }
            }

            if (humanCnt == 2 && emptyCnt == 1) {
                return true;
            }

            row++;
            col--;
        }

        return false;
    }

    /**
     * Tests whether placing the requested symbol at an empty candidate cell
     * would complete a consecutive three-cell pattern in the given orientation.
     *
     * @param row candidate row
     * @param col candidate column
     * @param board the current square game board
     * @param symbol symbol to test
     * @param operation orientation: {@code ROW}, {@code COLUMN},
     *                  {@code MAIN_DIAGONAL}, or {@code ANTI_DIAGONAL}
     * @return {@code true} if the placement completes a three-cell pattern
     */
    private boolean formsThree(int row, int col, String[][] board, String symbol, String operation) {
        if (board == null || symbol == null || row < 0 || col < 0
                || row >= board.length || col >= board[row].length) {
            return false;
        }
        int n = board.length;
        if (operation.equals("ROW")) {
            // X _ X
            if (inBounds(col - 1, n) && inBounds(col + 1, n) &&
                    symbol.equals(board[row][col - 1]) &&
                    symbol.equals(board[row][col + 1])) {
                return true;
            }

            // X X _
            if (inBounds(col - 2, n) && inBounds(col - 1, n) &&
                    symbol.equals(board[row][col - 1]) &&
                    symbol.equals(board[row][col - 2])) {
                return true;
            }

            // _ X X
            if (inBounds(col + 1, n) && inBounds(col + 2, n) &&
                    symbol.equals(board[row][col + 1]) &&
                    symbol.equals(board[row][col + 2])) {
                return true;
            }
        }
        if (operation.equals("COLUMN")) {

            // X
            // _
            // X
            if (inBounds(row - 1, n) && inBounds(row + 1, n) &&
                    symbol.equals(board[row - 1][col]) &&
                    symbol.equals(board[row + 1][col])) {
                return true;
            }

            // X
            // X
            // _
            if (inBounds(row - 1, n) && inBounds(row - 2, n) &&
                    symbol.equals(board[row - 1][col]) &&
                    symbol.equals(board[row - 2][col])) {
                return true;
            }

            // _
            // X
            // X
            if (inBounds(row + 1, n) && inBounds(row + 2, n) &&
                    symbol.equals(board[row + 1][col]) &&
                    symbol.equals(board[row + 2][col])) {
                return true;
            }
        }
        if (operation.equals("MAIN_DIAGONAL")) {

            // Only valid cells on main diagonal
            if (row != col) return false;

            // X _ X
            if (inBounds(row - 1, n) && inBounds(row + 1, n) &&
                    symbol.equals(board[row - 1][col - 1]) &&
                    symbol.equals(board[row + 1][col + 1])) {
                return true;
            }

            // X X _
            if (inBounds(row - 1, n) && inBounds(row - 2, n) &&
                    symbol.equals(board[row - 1][col - 1]) &&
                    symbol.equals(board[row - 2][col - 2])) {
                return true;
            }

            // _ X X
            if (inBounds(row + 1, n) && inBounds(row + 2, n) &&
                    symbol.equals(board[row + 1][col + 1]) &&
                    symbol.equals(board[row + 2][col + 2])) {
                return true;
            }
        }
        if (operation.equals("ANTI_DIAGONAL")) {

            // Only valid cells on anti-diagonal
            if (row + col != n - 1) return false;

            // X _ X
            if (inBounds(row - 1, n) && inBounds(col + 1, n) &&
                    inBounds(row + 1, n) && inBounds(col - 1, n) &&
                    symbol.equals(board[row - 1][col + 1]) &&
                    symbol.equals(board[row + 1][col - 1])) {
                return true;
            }

            // X X _
            if (inBounds(row - 1, n) && inBounds(col + 1, n) &&
                    inBounds(row - 2, n) && inBounds(col + 2, n) &&
                    symbol.equals(board[row - 1][col + 1]) &&
                    symbol.equals(board[row - 2][col + 2])) {
                return true;
            }

            // _ X X
            if (inBounds(row + 1, n) && inBounds(col - 1, n) &&
                    inBounds(row + 2, n) && inBounds(col - 2, n) &&
                    symbol.equals(board[row + 1][col - 1]) &&
                    symbol.equals(board[row + 2][col - 2])) {
                return true;
            }
        }


        return false;
    }

    /**
     * Checks whether an index is within a zero-based dimension.
     *
     * @param index index to check
     * @param n exclusive upper bound
     * @return {@code true} when {@code index} is between zero and {@code n - 1}
     */
    private boolean inBounds(int index, int n) {
        return index >= 0 && index < n;
    }


}
