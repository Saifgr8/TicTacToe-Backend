//package com.example.TicTacToe.bot;
//
//import com.example.TicTacToe.DTOs.BoardDTOs.DTOBoardMetrics;
//import com.example.TicTacToe.DTOs.GameDTOs.DTOGameMove;
//import com.example.TicTacToe.GAME.Enums.MoveType;
//import com.example.TicTacToe.GAME.Enums.PatternType;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class BotEngine {
//
//    public BotMove findWinningMove(String[][] board, String botSymbol) {
//        int n = board.length;
//
//        //Row
//        for (int row = 0; row < n; row++) {
//            int botCnt = 0;
//            int emptyCol = -1;
//
//            for (int col = 0; col < n; col++) {
//                if (botSymbol.equals(board[row][col])) {
//                    botCnt++;
//                } else if (isCellEmpty(board[row][col])) {
//                    emptyCol = col;
//                }
//            }
//            if (botCnt == n - 1 && emptyCol != -1) {
//                return new BotMove(
//                        row,
//                        PatternType.NONE,
//                        MoveType.NORMAL,
//                        emptyCol
//                );
//            }
//        }
//
//        //Column
//        for (int col = 0; col < n; col++) {
//            int botCnt = 0;
//            int emptyRow = -1;
//
//            for (int row = 0; row < n; row++) {
//                if (botSymbol.equals(board[row][col])) {
//                    botCnt++;
//                } else if (isCellEmpty(board[row][col])) {
//                    emptyRow = row;
//                }
//            }
//            if (botCnt == n - 1 && emptyRow != -1) {
//                return new BotMove(
//                        emptyRow,
//                        PatternType.NONE,
//                        MoveType.NORMAL,
//                        col
//                );
//            }
//        }
//
//        //Main Diagonal
//        int mainDiagonalRow = 0;
//        int mainDiagonalCol = 0;
//        int mainDiagonalBotCnt = 0;
//        int mainDiagonalEmpty = -1;
//        while (mainDiagonalRow < n && mainDiagonalCol < n) {
//
//            if (botSymbol.equals(board[mainDiagonalRow][mainDiagonalCol])) {
//                mainDiagonalBotCnt++;
//            } else if (isCellEmpty(board[mainDiagonalRow][mainDiagonalCol])) {
//                mainDiagonalEmpty = mainDiagonalCol;
//            }
//
//            mainDiagonalRow++;
//            mainDiagonalCol++;
//
//        }
//        if (mainDiagonalBotCnt < n - 1 && mainDiagonalEmpty != -1) {
//            return new BotMove(
//                    mainDiagonalRow,
//                    PatternType.NONE,
//                    MoveType.NORMAL,
//                    mainDiagonalCol
//            );
//        }
//        // Anti Diagonal
//        int row = 0;
//        int col = n - 1;
//
//        int botCount = 0;
//        int emptyRow = -1;
//        int emptyCol = -1;
//
//        while (row < n && col >= 0) {
//
//            if (botSymbol.equals(board[row][col])) {
//                botCount++;
//            } else if (isCellEmpty(board[row][col])) {
//                emptyRow = row;
//                emptyCol = col;
//            }
//
//            row++;
//            col--;
//        }
//
//        if (botCount == n - 1 && emptyRow != -1) {
//            return new BotMove(
//                    emptyRow,
//                    PatternType.NONE,
//                    MoveType.NORMAL,
//                    emptyCol
//            );
//        }
//
//        return null;
//    }
//
//    public BotMove findWinningMoveWithPoints(String[][] board, String botSymbol,
//                                             int botPoints, int humanPoints, String humanSymbol,
//                                             DTOGameMove prevMoveType) {
//        int n = board.length;
//        boolean isDoubleOverride = MoveType.DOUBLE_OVERRIDE.equals(prevMoveType.getMoveType());
//        //Row
//        for (int row = 0; row < n; row++) {
//            int botCnt = 0;
//            int emptyCol = -1;
//            int humanCnt = 0;
//            List<Integer> humanCell = new ArrayList<>();
//
//            for (int col = 0; col < n; col++) {
//                if (botSymbol.equals(board[row][col])) {
//                    botCnt++;
//                } else if (humanSymbol.equals(board[row][col])) {
//                    humanCnt++;
//                    humanCell.add(col);
//                } else if (isCellEmpty(board[row][col])) {
//                    emptyCol = col;
//                }
//            }
//            //Override
//            if (emptyCol == -1 && botCnt == n - 1 && humanCnt == 1 && botPoints >= 4) {
//                return new BotMove(
//                        row,
//                        PatternType.NONE,
//                        isDoubleOverride ? MoveType.DOUBLE_OVERRIDE : MoveType.OVERRIDE,
//                        humanCell.get(0)
//                );
//            }
//            //Double override
//            if (emptyCol == -1 && botCnt == n - 2 && humanCnt == 2 && botPoints >= 5) {
//                return new BotMove(
//                        row,
//                        PatternType.NONE,
//                        MoveType.DOUBLE_OVERRIDE,
//                        humanCell.get(0)
//                );
//            }
//        }
//
//        //Column
//        for (int col = 0; col < n; col++) {
//            int botCnt = 0;
//            int emptyRow = -1;
//            int humanCnt = 0;
//            List<Integer> humanCell = new ArrayList<>();
//
//            for (int row = 0; row < n; row++) {
//                if (botSymbol.equals(board[row][col])) {
//                    botCnt++;
//                } else if (humanSymbol.equals(board[row][col])) {
//                    humanCnt++;
//                    humanCell.add(col);
//                } else if (isCellEmpty(board[row][col])) {
//                    emptyRow = col;
//                }
//            }
//            //Override
//            if (emptyRow == -1 && botCnt == n - 1 && humanCnt == 1 && botPoints >= 4) {
//                return new BotMove(
//                        col,
//                        PatternType.NONE,
//                        isDoubleOverride ? MoveType.DOUBLE_OVERRIDE : MoveType.OVERRIDE,
//                        humanCell.get(0)
//                );
//            }
//            //Double Override
//            if (emptyRow == -1 && botCnt == n - 2 && humanCnt == 2 && botPoints >= 5) {
//                return new BotMove(
//                        col,
//                        PatternType.NONE,
//                        MoveType.DOUBLE_OVERRIDE,
//                        humanCell.get(0)
//                );
//            }
//        }
//
//        //Main Diagonal
//        int mainDiagonalRow = 0;
//        int mainDiagonalCol = 0;
//        int mainDiagonalBotCnt = 0;
//        int mainDiagonalEmpty = -1;
//        int humanCnt = 0;
//        List<Integer> humanCell = new ArrayList<>();
//
//        while (mainDiagonalRow < n && mainDiagonalCol < n) {
//
//            if (botSymbol.equals(board[mainDiagonalRow][mainDiagonalCol])) {
//                mainDiagonalBotCnt++;
//            } else if (humanSymbol.equals(board[mainDiagonalRow][mainDiagonalCol])) {
//                humanCnt++;
//                humanCell.add(mainDiagonalCol);
//            } else if (isCellEmpty(board[mainDiagonalRow][mainDiagonalCol])) {
//                mainDiagonalEmpty = mainDiagonalCol;
//            }
//
//            mainDiagonalRow++;
//            mainDiagonalCol++;
//
//        }
//        //Override
//        if (mainDiagonalEmpty == -1 && mainDiagonalBotCnt == n - 1 && humanCnt == 1 && botPoints >= 4) {
//            return new BotMove(
//                    mainDiagonalCol,
//                    PatternType.NONE,
//                    isDoubleOverride ? MoveType.DOUBLE_OVERRIDE : MoveType.OVERRIDE,
//                    humanCell.get(0)
//            );
//        }
//        //Double Override
//        if (mainDiagonalEmpty == -1 && mainDiagonalBotCnt == n - 2 && humanCnt == 2 && botPoints >= 5) {
//            return new BotMove(
//                    mainDiagonalCol,
//                    PatternType.NONE,
//                    MoveType.DOUBLE_OVERRIDE,
//                    humanCell.get(0)
//            );
//        }
//
//        // Anti Diagonal
//        int row = 0;
//        int col = n - 1;
//
//        int botCount = 0;
//        int humanRow = -1;
//        int humanCol = -1;
//        int antiHumanCnt = 0;
//        int antiEmptyRow = -1;
//        int antiEmptyCol = -1;
//        List<Integer> antiHumanCell = new ArrayList<>();
//
//        while (row < n && col >= 0) {
//
//            if (botSymbol.equals(board[row][col])) {
//                botCount++;
//            } else if (humanSymbol.equals(board[row][col])) {
//                antiHumanCnt++;
//                humanRow = row;
//                humanCol = col;
//            } else if (isCellEmpty(board[row][col])) {
//                antiEmptyRow = row;
//                antiEmptyCol = col;
//            }
//
//            row++;
//            col--;
//        }
//        //Override
//        if (antiEmptyRow == -1 && antiEmptyCol == -1 &&
//                botCount == n - 1 && antiHumanCnt == 1 && botPoints >= 4) {
//            return new BotMove(
//                    humanRow,
//                    PatternType.NONE,
//                    isDoubleOverride ? MoveType.DOUBLE_OVERRIDE : MoveType.OVERRIDE,
//                    humanCol
//            );
//        }
//        //Double Override
//        if (antiEmptyRow == -1 && antiEmptyCol == -1 &&
//                botCount == n - 2 && antiHumanCnt == 2 && botPoints >= 4) {
//            return new BotMove(
//                    humanRow,
//                    PatternType.NONE,
//                    MoveType.DOUBLE_OVERRIDE,
//                    humanCol
//            );
//        }
//
//        return null;
//    }
//
//    public BotMove blockWinningMove(String[][] board, String humanSymbol) {
//        int n = board.length;
//
//        //Row
//        for (int row = 0; row < n; row++) {
//            int humanCnt = 0;
//            int emptyCol = -1;
//
//            for (int col = 0; col < n; col++) {
//                if (humanSymbol.equals(board[row][col])) {
//                    humanCnt++;
//                } else if (isCellEmpty(board[row][col])) {
//                    emptyCol = col;
//                }
//            }
//            if (humanCnt == n - 1 && emptyCol != -1) {
//                return new BotMove(
//                        row,
//                        PatternType.NONE,
//                        MoveType.NORMAL,
//                        emptyCol
//                );
//            }
//        }
//
//        //Column
//        for (int col = 0; col < n; col++) {
//            int humanCnt = 0;
//            int emptyRow = -1;
//
//            for (int row = 0; row < n; row++) {
//                if (humanSymbol.equals(board[row][col])) {
//                    humanCnt++;
//                } else if (isCellEmpty(board[row][col])) {
//                    emptyRow = row;
//                }
//            }
//            if (humanCnt == n - 1 && emptyRow != -1) {
//                return new BotMove(
//                        emptyRow,
//                        PatternType.NONE,
//                        MoveType.NORMAL,
//                        col
//                );
//            }
//        }
//
//        //Main Diagonal
//        int mainDiagonalRow = 0;
//        int mainDiagonalCol = 0;
//        int mainDiagonalHumanCnt = 0;
//        int mainDiagonalEmpty = -1;
//        while (mainDiagonalRow < n && mainDiagonalCol < n) {
//
//            if (humanSymbol.equals(board[mainDiagonalRow][mainDiagonalCol])) {
//                mainDiagonalHumanCnt++;
//            } else if (isCellEmpty(board[mainDiagonalRow][mainDiagonalCol])) {
//                mainDiagonalEmpty = mainDiagonalCol;
//            }
//
//            mainDiagonalRow++;
//            mainDiagonalCol++;
//
//        }
//        if (mainDiagonalHumanCnt < n - 1 && mainDiagonalEmpty != -1) {
//            return new BotMove(
//                    mainDiagonalRow,
//                    PatternType.NONE,
//                    MoveType.NORMAL,
//                    mainDiagonalCol
//            );
//        }
//        // Anti Diagonal
//        int row = 0;
//        int col = n - 1;
//
//        int humanCount = 0;
//        int emptyRow = -1;
//        int emptyCol = -1;
//
//        while (row < n && col >= 0) {
//
//            if (humanSymbol.equals(board[row][col])) {
//                humanCount++;
//            } else if (isCellEmpty(board[row][col])) {
//                emptyRow = row;
//                emptyCol = col;
//            }
//
//            row++;
//            col--;
//        }
//
//        if (humanCount == n - 1 && emptyRow != -1) {
//            return new BotMove(
//                    emptyRow,
//                    PatternType.NONE,
//                    MoveType.NORMAL,
//                    emptyCol
//            );
//        }
//
//        return null;
//    }
//
//    public BotMove blockWinningMoveWithPoints(String[][] board, String humanSymbol, int botPoints
//            , int humanPoints, String botSymbol) {
//        boolean canScore = canHumanScore(board, humanSymbol, botSymbol);
//        int n = board.length;
//
//        //Row
//        for (int row = 0; row < n; row++) {
//            int botCnt = 0;
//            int emptyCol = -1;
//            int humanCnt = 0;
//            List<Integer> humanCell = new ArrayList<>();
//
//            for (int col = 0; col < n; col++) {
//                if (botSymbol.equals(board[row][col])) {
//                    botCnt++;
//                } else if (humanSymbol.equals(board[row][col])) {
//                    humanCnt++;
//                    humanCell.add(col);
//                } else if (isCellEmpty(board[row][col])) {
//                    emptyCol = col;
//                }
//            }
//            //Override and double override
//            if ((emptyCol == -1 && humanCnt == n - 1 && botCnt == 1 && (humanPoints >= 4 ||
//                    (humanPoints == 3 && canScore) && botPoints >= 2)
//                    ||
//                    ((emptyCol == -1 && humanCnt == n - 2 && botCnt == 2 && (humanPoints >= 5 ||
//                            (humanPoints == 4 && canScore) && botPoints >= 2)
//                    )))) {
//                return new BotMove(
//                        row,
//                        PatternType.ROW,
//                        MoveType.PATTERN_BOMB,
//                        humanCell.get(0)
//                );
//            }
//
//        }
//
//        //Column
//        for (int col = 0; col < n; col++) {
//            int botCnt = 0;
//            int emptyRow = -1;
//            int humanCnt = 0;
//            List<Integer> humanCell = new ArrayList<>();
//
//            for (int row = 0; row < n; row++) {
//                if (botSymbol.equals(board[row][col])) {
//                    botCnt++;
//                } else if (humanSymbol.equals(board[row][col])) {
//                    humanCnt++;
//                    humanCell.add(col);
//                } else if (isCellEmpty(board[row][col])) {
//                    emptyRow = col;
//                }
//            }
//            //Override and double override
//            if ((emptyRow == -1 && humanCnt == n - 1 && botCnt == 1 && (humanPoints >= 4 ||
//                    (humanPoints == 3 && canScore) && botPoints >= 2)
//                    ||
//                    ((emptyRow == -1 && humanCnt == n - 2 && botCnt == 2 && (humanPoints >= 5 ||
//                            (humanPoints == 4 && canScore) && botPoints >= 2)
//                    )))) {
//                return new BotMove(
//                        col,
//                        PatternType.COLUMN,
//                        MoveType.PATTERN_BOMB,
//                        humanCell.get(0)
//                );
//            }
//        }
//
//        //Main Diagonal
//        int mainDiagonalRow = 0;
//        int mainDiagonalCol = 0;
//        int mainDiagonalBotCnt = 0;
//        int mainDiagonalEmpty = -1;
//        int humanCnt = 0;
//        List<Integer> humanCell = new ArrayList<>();
//
//        while (mainDiagonalRow < n && mainDiagonalCol < n) {
//
//            if (botSymbol.equals(board[mainDiagonalRow][mainDiagonalCol])) {
//                mainDiagonalBotCnt++;
//            } else if (humanSymbol.equals(board[mainDiagonalRow][mainDiagonalCol])) {
//                humanCnt++;
//                humanCell.add(mainDiagonalCol);
//            } else if (isCellEmpty(board[mainDiagonalRow][mainDiagonalCol])) {
//                mainDiagonalEmpty = mainDiagonalCol;
//            }
//
//            mainDiagonalRow++;
//            mainDiagonalCol++;
//
//        }
//        //Override and double override
//        if ((mainDiagonalEmpty == -1 && humanCnt == n - 1 && mainDiagonalBotCnt == 1 && (humanPoints >= 4 ||
//                (humanPoints == 3 && canScore) && botPoints >= 2)
//                ||
//                ((mainDiagonalEmpty == -1 && humanCnt == n - 2 && mainDiagonalBotCnt == 2 && (humanPoints >= 5 ||
//                        (humanPoints == 4 && canScore) && botPoints >= 2)
//                )))) {
//            return new BotMove(
//                    0,
//                    PatternType.LEFT_DIAGONAL,
//                    MoveType.PATTERN_BOMB,
//                    0
//            );
//        }
//
//        // Anti Diagonal
//        int row = 0;
//        int col = n - 1;
//
//        int botCount = 0;
//        int humanRow = -1;
//        int humanCol = -1;
//        int antiHumanCnt = 0;
//        int antiEmptyRow = -1;
//        int antiEmptyCol = -1;
//
//        while (row < n && col >= 0) {
//
//            if (botSymbol.equals(board[row][col])) {
//                botCount++;
//            } else if (humanSymbol.equals(board[row][col])) {
//                antiHumanCnt++;
//                humanRow = row;
//                humanCol = col;
//            } else if (isCellEmpty(board[row][col])) {
//                antiEmptyRow = row;
//                antiEmptyCol = col;
//            }
//
//            row++;
//            col--;
//        }
//        //Override and double override
//        if ((antiEmptyRow == -1 && antiHumanCnt == n - 1 && botCount == 1 && (humanPoints >= 4 ||
//                (humanPoints == 3 && canScore) && botPoints >= 2)
//                ||
//                ((antiEmptyRow == -1 && antiHumanCnt == n - 2 && botCount == 2 && (humanPoints >= 5 ||
//                        (humanPoints == 4 && canScore) && botPoints >= 2)
//                )))) {
//            return new BotMove(
//                    0,
//                    PatternType.RIGHT_DIAGONAL,
//                    MoveType.PATTERN_BOMB,
//                    n - 1
//            );
//        }
//        return null;
//    }
//
//    private boolean canHumanScore(String[][] board, String humanSymbol, String botSymbol) {
//        int n = board.length;
//        if (findBoardHealth(board, humanSymbol, botSymbol) <= 40.0) {
//            return true;
//        }
//        //Rows
//        for (String[] strings : board) {
//
//            for (int j = 0; j < strings.length - 3; j++) {
//                int humanCnt = 0;
//                int emptyCnt = 0;
//
//                String first = strings[j];
//                String second = strings[j + 1];
//                String third = strings[j + 2];
//
//                String[] window = {first, second, third};
//
//                for (String item : window) {
//                    if (item.equals(humanSymbol)) {
//                        humanCnt++;
//                    } else if (item.isEmpty()) {
//                        emptyCnt++;
//                    }
//                }
//                if (humanCnt == 2 && emptyCnt == 1) return true;
//            }
//        }
//
//        //Columns
//        for (int col = 0; col < n; col++) {
//
//            for (int j = 0; j < board.length - 3; j++) {
//                int humanCnt = 0;
//                int emptyCnt = 0;
//
//                String first = board[j][col];
//                String second = board[j + 1][col];
//                String third = board[j + 2][col];
//
//                String[] window = {first, second, third};
//
//                for (String item : window) {
//                    if (item.equals(humanSymbol)) {
//                        humanCnt++;
//                    } else if (item.isEmpty()) {
//                        emptyCnt++;
//                    }
//                }
//                if (humanCnt == 2 && emptyCnt == 1) return true;
//            }
//        }
//
//        //Main Diagonal
//        for (int startRow = 0; startRow < n; startRow++) {
//            if (checkLeftDiagonalFrom(startRow, 0, board, humanSymbol)) return true;
//        }
//        //Left Diagonal
//        for (int startCol = 1; startCol < n; startCol++) {
//            if (checkLeftDiagonalFrom(0, startCol, board, humanSymbol)) return true;
//        }
//
//        //Anti Diagonal
//        for (int startRow = 0; startRow < n; startRow++) {
//            if (checkRightDiagonalFrom(startRow, n - 1, board, humanSymbol)) return true;
//        }
//        //Right Diagonal
//        for (int startCol = n - 2; startCol >= 0; startCol--) {
//            if (checkRightDiagonalFrom(0, startCol, board, humanSymbol)) return true;
//        }
//        return false;
//    }
//
//    public double findBoardHealth(String[][] board, String botSymbol, String humanSymbol) {
//        int size = board.length;
//        int botCnt = 0;
//        int humanCnt = 0;
//        int open = 0;
//        for (String[] row : board) {
//            for (String item : row) {
//                if (item == null) {
//                    open++;
//                } else if (item.equals(botSymbol)) {
//                    botCnt++;
//                } else if (item.equals(humanSymbol)) {
//                    humanCnt++;
//                }
//            }
//        }
//        int totalCells = size * size;
//        double humanPercentage = ((double) humanCnt / totalCells) * 100;
//        return 100.0 - humanPercentage;
//    }
//
//    public boolean canScoreDuringTwist(String[][] board, String botSymbol) {
//        return false;
//    }
//
//    public BotMove playTwistMove(String[][] board) {
//        return null;
//    }
//
//    public DTOBoardMetrics getBoardMetrics(String[][] board, String humanSymbol, int humanPoints, String botSymbol) {
//        int n = board.length;
//        int rowCnt = 0;
//        boolean canScore = canHumanScore(board, humanSymbol, botSymbol);
//
//        //Row
//        for (String[] row : board) {
//            String rowComment = "";
//            int humanCnt = 0;
//            int rowIndex = 0;
//
//            for (String item : row) {
//                if (item.equals(humanSymbol)) humanCnt++;
//            }
//            if ((humanCnt == n - 1 && humanPoints >= 4) || (humanCnt == n - 2 && canScore && humanPoints >= 5)) {
//                rowComment = "CRITICAL";
//                rowIndex = rowCnt;
//                return new DTOBoardMetrics(
//                        -1, -1, -1, rowIndex,
//                        rowComment, "", "", ""
//                );
//            }
//            rowCnt++;
//        }
//
//        //Col
//        for (int col = 0; col < n; col++) {
//            String colComment = "";
//            int humanCnt = 0;
//            int colIndex = 0;
//
//            List<String> data = new ArrayList<>();
//            for (int row = 0; row < n; row++) {
//                data.add(board[row][col]);
//            }
//            for (String item : data) {
//                if (item.equals(humanSymbol)) humanCnt++;
//            }
//            if ((humanCnt == n - 1 && humanPoints >= 4) || (humanCnt == n - 2 && canScore && humanPoints >= 5)) {
//                colComment = "CRITICAL";
//                colIndex = col;
//                return new DTOBoardMetrics(
//                        -1, colIndex, -1, -1,
//                        "", colComment, "", ""
//                );
//            }
//            rowCnt++;
//        }
//
//        // Left Diagonals (\)
//        int leftDiagIndex = 0;
//
//        // Start from left column
//        for (int startRow = 0; startRow < n; startRow++) {
//
//            int humanCnt = 0;
//            int row = startRow, col = 0;
//
//            while (row < n && col < n) {
//                if (board[row][col].equals(humanSymbol)) {
//                    humanCnt++;
//                }
//                row++;
//                col++;
//            }
//
//            if ((humanCnt == n - 1 && humanPoints >= 4) ||
//                    (humanCnt == n - 2 && canScore && humanPoints >= 5)) {
//
//                return new DTOBoardMetrics(
//                        -1, -1, -1, leftDiagIndex,
//                        "", "", "CRITICAL", ""
//                );
//            }
//
//            leftDiagIndex++;
//        }
//
//        // Start from top row (excluding main diagonal)
//        for (int startCol = 1; startCol < n; startCol++) {
//
//            int humanCnt = 0;
//            int row = 0, col = startCol;
//
//            while (row < n && col < n) {
//                if (board[row][col].equals(humanSymbol)) {
//                    humanCnt++;
//                }
//                row++;
//                col++;
//            }
//
//            if ((humanCnt == n - 1 && humanPoints >= 4) ||
//                    (humanCnt == n - 2 && canScore && humanPoints >= 5)) {
//
//                return new DTOBoardMetrics(
//                        -1, -1, -1, leftDiagIndex,
//                        "", "", "CRITICAL", ""
//                );
//            }
//
//            leftDiagIndex++;
//        }
//
//        // Right Diagonals (/)
//        int rightDiagIndex = 0;
//
//        // Start from right column
//        for (int startRow = 0; startRow < n; startRow++) {
//
//            int humanCnt = 0;
//            int row = startRow, col = n - 1;
//
//            while (row < n && col >= 0) {
//                if (board[row][col].equals(humanSymbol)) {
//                    humanCnt++;
//                }
//                row++;
//                col--;
//            }
//
//            if ((humanCnt == n - 1 && humanPoints >= 4) ||
//                    (humanCnt == n - 2 && canScore && humanPoints >= 5)) {
//
//                return new DTOBoardMetrics(
//                        rightDiagIndex, -1, -1, -1,
//                        "", "", "", "CRITICAL"
//                );
//            }
//
//            rightDiagIndex++;
//        }
//
//        // Start from top row (excluding main anti-diagonal)
//        for (int startCol = n - 2; startCol >= 0; startCol--) {
//
//            int humanCnt = 0;
//            int row = 0, col = startCol;
//
//            while (row < n && col >= 0) {
//                if (board[row][col].equals(humanSymbol)) {
//                    humanCnt++;
//                }
//                row++;
//                col--;
//            }
//
//            if ((humanCnt == n - 1 && humanPoints >= 4) ||
//                    (humanCnt == n - 2 && canScore && humanPoints >= 5)) {
//
//                return new DTOBoardMetrics(
//                        rightDiagIndex, -1, -1, -1,
//                        "", "", "", "CRITICAL"
//                );
//            }
//
//            rightDiagIndex++;
//        }
//
//
//        return null;
//    }
//
//    public BotMove findBestMove(String[][] board, String humanSymbol, String botSymbol) {
//        int n = board.length;
//
//        //n-2 handling
//
//        //ROW - Score first, Block second
//        for (int row = 0; row < n; row++) {
//
//            int scoreIndex = -1;
//            int blockIndex = -1;
//            String operation = "ROW";
//
//            for (int col = 0; col < n; col++) {
//
//                if (!isCellEmpty(board[row][col])) continue;
//
//                // ===== BOT SCORING CHECK =====
//                if (scoreIndex == -1 && formsThree(row, col, board, botSymbol, operation)) {
//                    scoreIndex = col;
//                }
//
//                // ===== HUMAN BLOCK CHECK =====
//                if (blockIndex == -1 && formsThree(row, col, board, humanSymbol, operation)) {
//                    blockIndex = col;
//                }
//
//                // Early exit
//                if (scoreIndex != -1) break;
//            }
//
//            // Priority: SCORE > BLOCK
//            if (scoreIndex != -1) {
//                return new BotMove(
//                        row,
//                        PatternType.NONE,
//                        MoveType.NORMAL,
//                        scoreIndex
//                );
//            }
//
//            if (blockIndex != -1) {
//                return new BotMove(
//                        row,
//                        PatternType.NONE,
//                        MoveType.NORMAL,
//                        blockIndex
//                );
//            }
//        }
//
//        //Column - Score first, Block second
//        for (int col = 0; col < n; col++) {
//
//            int scoreIndex = -1;
//            int blockIndex = -1;
//            String operation = "COLUMN";
//
//            for (int row = 0; row < n; row++) {
//
//                if (!isCellEmpty(board[row][col])) continue;
//
//                // ===== BOT SCORING CHECK =====
//                if (scoreIndex == -1 && formsThree(row, col, board, botSymbol, operation)) {
//                    scoreIndex = row;
//                }
//
//                // ===== HUMAN BLOCK CHECK =====
//                if (blockIndex == -1 && formsThree(row, col, board, humanSymbol, operation)) {
//                    blockIndex = row;
//                }
//
//                // Early exit
//                if (scoreIndex != -1) break;
//            }
//
//            // Priority: SCORE > BLOCK
//            if (scoreIndex != -1) {
//                return new BotMove(
//                        col,
//                        PatternType.NONE,
//                        MoveType.NORMAL,
//                        scoreIndex
//                );
//            }
//
//            if (blockIndex != -1) {
//                return new BotMove(
//                        col,
//                        PatternType.NONE,
//                        MoveType.NORMAL,
//                        blockIndex
//                );
//            }
//        }
//
//        // MAIN DIAGONAL — Score first, Block second
//        int mainScoreIndex = -1;
//        int mainBlockIndex = -1;
//        for (int i = 0; i < n; i++) {
//
//            if (!isCellEmpty(board[i][i])) continue;
//
//            if (mainScoreIndex == -1 &&
//                    formsThree(i, i, board, botSymbol, "MAIN_DIAGONAL")) {
//                mainScoreIndex = i;
//            }
//
//            if (mainBlockIndex == -1 &&
//                    formsThree(i, i, board, humanSymbol, "MAIN_DIAGONAL")) {
//                mainBlockIndex = i;
//            }
//
//            if (mainScoreIndex != -1) break;
//        }
//        if (mainScoreIndex != -1) {
//            return new BotMove(
//                    -1,               // diagonal
//                    PatternType.NONE,
//                    MoveType.NORMAL,
//                    mainScoreIndex
//            );
//        }
//        if (mainBlockIndex != -1) {
//            return new BotMove(
//                    -1,
//                    PatternType.NONE,
//                    MoveType.NORMAL,
//                    mainBlockIndex
//            );
//        }
//
//        // ANTI-DIAGONAL — Score first, Block second
//        int scoreIndex = -1;
//        int blockIndex = -1;
//        for (int row = 0; row < n; row++) {
//            int col = n - 1 - row;
//
//            if (!isCellEmpty(board[row][col])) continue;
//
//            if (scoreIndex == -1 &&
//                    formsThree(row, col, board, botSymbol, "ANTI_DIAGONAL")) {
//                scoreIndex = row;
//            }
//
//            if (blockIndex == -1 &&
//                    formsThree(row, col, board, humanSymbol, "ANTI_DIAGONAL")) {
//                blockIndex = row;
//            }
//
//            if (scoreIndex != -1) break;
//        }
//        if (scoreIndex != -1) {
//            return new BotMove(
//                    -2,               // anti-diagonal identifier (your choice)
//                    PatternType.NONE,
//                    MoveType.NORMAL,
//                    scoreIndex
//            );
//        }
//        if (blockIndex != -1) {
//            return new BotMove(
//                    -2,
//                    PatternType.NONE,
//                    MoveType.NORMAL,
//                    blockIndex
//            );
//        }
//
//        //Immediate Score        +5
//        //Immediate Block        +4
//        //Advantage              +3
//        //Opponent Advantage     -3
//        //Multi Score            +10
//
//        //Multi Block            +8
//        int bestScore = Integer.MIN_VALUE;
//        int bestX = -1;
//        int bestY = -1;
//        BotMove bestMove = null;
//
//        //ROWS
//        for (int row = 0; row < n; row++) {
//            int score = 0;
//
//            for (int col = 0; col < n; col++) {
//                // Immediate score
//                score += countImmediateScore(row, col, botSymbol, "ROW", board) * 5;
//                // Immediate score
//                score += countImmediateScore(row, col, humanSymbol, "ROW", board) * 4;
//                // Advantage
//                score += countAdvantages(row, col, botSymbol, "ROW", board) * 3;
//                // DisAdvantage
//                score += countAdvantages(row, col, humanSymbol, "ROW", board) * 3;
//
//                // 5️⃣ Multi-score
//                if (createsMultiScore(row, col, botSymbol, "ROW", board)) score += 10;
//                if (blocksMultiScore(row, col, humanSymbol, "ROW", board)) score += 8;
//
//                if (score > bestScore) {
//                    bestScore = score;
//                    bestMove = new BotMove(row, PatternType.NONE, MoveType.NORMAL, col);
//                }
//            }
//        }
//        //Columns
//        for (int col = 0; col < n; col++) {
//            int score = 0;
//            for (int row = 0; row < n; row++) {
//                // Immediate score
//                score += countImmediateScore(row, col, botSymbol, "COLUMN", board) * 5;
//                // Immediate score
//                score += countImmediateScore(row, col, humanSymbol, "COLUMN", board) * 4;
//                // Advantage
//                score += countAdvantages(row, col, botSymbol, "COLUMN", board) * 3;
//                // DisAdvantage
//                score += countAdvantages(row, col, humanSymbol, "COLUMN", board) * 3;
//
//                // 5️⃣ Multi-score
//                if (createsMultiScore(row, col, botSymbol, "COLUMN", board)) score += 10;
//                if (blocksMultiScore(row, col, humanSymbol, "COLUMN", board)) score += 8;
//
//                if (score > bestScore) {
//                    bestScore = score;
//                    bestMove = new BotMove(row, PatternType.NONE, MoveType.NORMAL, col);
//                }
//            }
//        }
//    }
//
//    private int countImmediateScore(int row, int col, String symbol, String row1, String[][] board) {
//        boolean canScore = formsThree(row,col,board, symbol, row1);
//        return canScore ? 1 : 0;
//    }
//
//    private int countAdvantages(int row, int col, String symbol, String row1, String[][] board) {
//        int n = board.length;
//        if (row == 0 || col == 0) return 0;
//        if (row == board.length-1 || col == board.length-1) return 0;
//        if (!isCellEmpty(board[row][col])) return 0;
//
//        if (row1.equals("ROW")){
//            //_ X _ _
//            if (inBounds(col + 2, n) && inBounds(col - 1, n) &&
//                    board[row][col - 1].isEmpty() &&
//                    board[row][col - 2].equals(symbol)) {
//                    return true;
//            }
//        }
//
//    }
//
//    private boolean blocksMultiScore(int row, int col, String humanSymbol, String column, String[][] board) {
//    }
//
//    private boolean createsMultiScore(int row, int col, String botSymbol, String column, String[][] board) {
//    }
//
//
//    public BotMove randomSafeMove(String[][] board) {
//        return null;
//    }
//
//    private boolean isCellEmpty(String cell) {
//        return cell == null || cell.isEmpty();
//    }
//
//    private boolean checkLeftDiagonalFrom(
//            int row,
//            int col,
//            String[][] board,
//            String humanSymbol
//    ) {
//        int n = board.length;
//
//        while (row + 2 < n && col + 2 < n) {
//
//            int humanCnt = 0;
//            int emptyCnt = 0;
//
//            String first = board[row][col];
//            String second = board[row + 1][col + 1];
//            String third = board[row + 2][col + 2];
//
//            String[] window = {first, second, third};
//
//            for (String item : window) {
//                if (item.equals(humanSymbol)) {
//                    humanCnt++;
//                } else if (item.isEmpty()) {
//                    emptyCnt++;
//                }
//            }
//
//            if (humanCnt == 2 && emptyCnt == 1) {
//                return true;
//            }
//
//            row++;
//            col++;
//        }
//
//        return false;
//    }
//
//    private boolean checkRightDiagonalFrom(
//            int row,
//            int col,
//            String[][] board,
//            String humanSymbol
//    ) {
//        int n = board.length;
//
//        while (row + 2 < n && col - 2 >= 0) {
//
//            int humanCnt = 0;
//            int emptyCnt = 0;
//
//            String first = board[row][col];
//            String second = board[row + 1][col - 1];
//            String third = board[row + 2][col - 2];
//
//            String[] window = {first, second, third};
//
//            for (String item : window) {
//                if (item.equals(humanSymbol)) {
//                    humanCnt++;
//                } else if (item.isEmpty()) {
//                    emptyCnt++;
//                }
//            }
//
//            if (humanCnt == 2 && emptyCnt == 1) {
//                return true;
//            }
//
//            row++;
//            col--;
//        }
//
//        return false;
//    }
//
//    private boolean formsThree(int row, int col, String[][] board, String symbol, String operation) {
//        int n = board.length;
//        if (operation.equals("ROW")) {
//            // X _ X
//            if (inBounds(col - 1, n) && inBounds(col + 1, n) &&
//                    board[row][col - 1].equals(symbol) &&
//                    board[row][col + 1].equals(symbol)) {
//                return true;
//            }
//
//            // X X _
//            if (inBounds(col - 2, n) && inBounds(col - 1, n) &&
//                    board[row][col - 1].equals(symbol) &&
//                    board[row][col - 2].equals(symbol)) {
//                return true;
//            }
//
//            // _ X X
//            if (inBounds(col + 1, n) && inBounds(col + 2, n) &&
//                    board[row][col + 1].equals(symbol) &&
//                    board[row][col + 2].equals(symbol)) {
//                return true;
//            }
//        }
//        if (operation.equals("COLUMN")) {
//
//            // X
//            // _
//            // X
//            if (inBounds(row - 1, n) && inBounds(row + 1, n) &&
//                    board[row - 1][col].equals(symbol) &&
//                    board[row + 1][col].equals(symbol)) {
//                return true;
//            }
//
//            // X
//            // X
//            // _
//            if (inBounds(row - 1, n) && inBounds(row - 2, n) &&
//                    board[row - 1][col].equals(symbol) &&
//                    board[row - 2][col].equals(symbol)) {
//                return true;
//            }
//
//            // _
//            // X
//            // X
//            if (inBounds(row + 1, n) && inBounds(row + 2, n) &&
//                    board[row + 1][col].equals(symbol) &&
//                    board[row + 2][col].equals(symbol)) {
//                return true;
//            }
//        }
//        if (operation.equals("MAIN_DIAGONAL")) {
//
//            // Only valid cells on main diagonal
//            if (row != col) return false;
//
//            // X _ X
//            if (inBounds(row - 1, n) && inBounds(row + 1, n) &&
//                    board[row - 1][col - 1].equals(symbol) &&
//                    board[row + 1][col + 1].equals(symbol)) {
//                return true;
//            }
//
//            // X X _
//            if (inBounds(row - 1, n) && inBounds(row - 2, n) &&
//                    board[row - 1][col - 1].equals(symbol) &&
//                    board[row - 2][col - 2].equals(symbol)) {
//                return true;
//            }
//
//            // _ X X
//            if (inBounds(row + 1, n) && inBounds(row + 2, n) &&
//                    board[row + 1][col + 1].equals(symbol) &&
//                    board[row + 2][col + 2].equals(symbol)) {
//                return true;
//            }
//        }
//        if (operation.equals("ANTI_DIAGONAL")) {
//
//            // Only valid cells on anti-diagonal
//            if (row + col != n - 1) return false;
//
//            // X _ X
//            if (inBounds(row - 1, n) && inBounds(col + 1, n) &&
//                    inBounds(row + 1, n) && inBounds(col - 1, n) &&
//                    board[row - 1][col + 1].equals(symbol) &&
//                    board[row + 1][col - 1].equals(symbol)) {
//                return true;
//            }
//
//            // X X _
//            if (inBounds(row - 1, n) && inBounds(col + 1, n) &&
//                    inBounds(row - 2, n) && inBounds(col + 2, n) &&
//                    board[row - 1][col + 1].equals(symbol) &&
//                    board[row - 2][col + 2].equals(symbol)) {
//                return true;
//            }
//
//            // _ X X
//            if (inBounds(row + 1, n) && inBounds(col - 1, n) &&
//                    inBounds(row + 2, n) && inBounds(col - 2, n) &&
//                    board[row + 1][col - 1].equals(symbol) &&
//                    board[row + 2][col - 2].equals(symbol)) {
//                return true;
//            }
//        }
//
//
//        return false;
//    }
//
//    private boolean inBounds(int index, int n) {
//        return index >= 0 && index < n;
//    }
//
//
//}
