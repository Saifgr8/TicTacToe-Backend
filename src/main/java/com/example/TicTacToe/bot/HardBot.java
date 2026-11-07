//package com.example.TicTacToe.bot;
//
//import com.example.TicTacToe.DTOs.BoardDTOs.DTOBoardMetrics;
//import com.example.TicTacToe.DTOs.GameDTOs.DTOGameMove;
//import com.example.TicTacToe.GAME.Enums.MoveType;
//import com.example.TicTacToe.GAME.Enums.PatternType;
//import com.example.TicTacToe.GAME.GameSession;
//
//public class HardBot extends BotEngine {
//    public BotMove decideMove(GameSession session, DTOGameMove prevMove) {
//        String[][] board = session.getBoard();
//        int size = board.length;
//
//
//        String botPlayer = session.getCurrentPlayer();
//        String humanPlayer =
//                botPlayer.equals(session.getPlayer1Username())
//                        ? session.getPlayer2Username()
//                        : session.getPlayer1Username();
//
//        String botSymbol = session.getSymbolForPlayer(botPlayer);
//        String humanSymbol = session.getSymbolForPlayer(humanPlayer);
//
//        int humanPoints = session.getCurrentPlayerPoints(humanPlayer);
//        int botPoints = session.getCurrentPlayerPoints(botPlayer);
//
//        //Immediate Win;
//        BotMove winMove = findWinningMove(board, botSymbol);
//        if (winMove != null) return winMove;
//
//        //Immediate Win;
//        BotMove winMovePoints = findWinningMoveWithPoints(board, botSymbol, botPoints, humanPoints, humanSymbol
//        ,prevMove);
//        if (winMovePoints != null) return winMovePoints;
//
//        //Block Opponent Win
//        BotMove blockMove = blockWinningMove(board, humanSymbol);
//        if (blockMove != null) return blockMove;
//
//        BotMove blockMovePoints = blockWinningMoveWithPoints(board, humanSymbol, botPoints, humanPoints, botSymbol);
//        if (blockMovePoints != null) return blockMovePoints;
//
//        //Twist usage
//        double boardHealth = findBoardHealth(board, botSymbol, humanSymbol);
//        if (!session.isTwistActive()
//                && botPoints >= 4
//                && boardHealth < 45
//                && canScoreDuringTwist(board, botSymbol)) {
//            return playTwistMove(board);
//        }
//
//        //Pattern Bomb - Deny opponent growth
//        DTOBoardMetrics boardMetrics = getBoardMetrics(board, humanSymbol, humanPoints, botSymbol);
//        //ROW BOMB
//        if (size >= 5 && botPoints >= 5 && boardMetrics.getRowComment().equals("CRITICAL")) {
//            return new BotMove(
//                    boardMetrics.getRowIndex(),
//                    PatternType.ROW,
//                    MoveType.PATTERN_BOMB,
//                    0
//            );
//        }
//        //Column BOMB
//        if (size >= 5 && botPoints >= 5 && boardMetrics.getColComment().equals("CRITICAL")) {
//            return new BotMove(
//                    0,
//                    PatternType.COLUMN,
//                    MoveType.PATTERN_BOMB,
//                    boardMetrics.getColIndex()
//            );
//        }
//        //LeftDiagonal BOMB
//        if (size >= 5 && botPoints >= 5 && boardMetrics.getLeftDiagonalComment().equals("CRITICAL")) {
//            return new BotMove(
//                    boardMetrics.getLeftDiagonalIndex(),
//                    PatternType.LEFT_DIAGONAL,
//                    MoveType.PATTERN_BOMB,
//                    0
//            );
//        }
//        //RightDiagonal BOMB
//        if (size >= 5 && botPoints >= 5 &&  boardMetrics.getRightDiagonalComment().equals("CRITICAL")) {
//            return new BotMove(
//                    boardMetrics.getRightDiagonalIndex(),
//                    PatternType.RIGHT_DIAGONAL,
//                    MoveType.PATTERN_BOMB,
//                    0
//            );
//        }
//
//        // Point based move
//        BotMove bestMove = findBestMove(board, humanSymbol, botSymbol);
//
//        return randomSafeMove(board);
//
//    }
//
//
//}
