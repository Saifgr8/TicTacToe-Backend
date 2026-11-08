package com.example.TicTacToe.GAME;

import com.example.TicTacToe.DTOs.GameDTOs.DTOGameSessionResponse;

import java.util.ArrayList;

public final class GameSessionSnapshot {
    private GameSessionSnapshot() {
    }

    public static DTOGameSessionResponse from(GameSession session) {
        synchronized (session) {
            DTOGameSessionResponse response = new DTOGameSessionResponse();
            response.setSessionId(session.getSessionId());
            String[][] board = session.getBoard();
            String[][] boardCopy = new String[board.length][];
            for (int i = 0; i < board.length; i++) {
                boardCopy[i] = board[i].clone();
            }
            response.setBoard(boardCopy);
            response.setGameStatus(session.getGameStatus());
            response.setCurrentPlayer(session.getCurrentPlayer());
            response.setWinner(session.getWinner());
            response.setDoubleOverrideActive(session.isDoubleOverride());
            response.setDoubleOverrideTurnsLeft(session.getDoubleOverrideTurns());
            response.setThreePatterns(new ArrayList<>(session.getNewPattern()));
            response.setWinPattern(new ArrayList<>(session.getWinPattern()));
            response.setPlayer1Points(session.getPlayer1Points());
            response.setPlayer1Symbol(session.getPlayer1Symbol());
            response.setPlayer1TwistTurns(session.getPlayer1TwistTurnsLeft());
            response.setPlayer2Points(session.getPlayer2Points());
            response.setPlayer2Symbol(session.getPlayer2Symbol());
            response.setPlayer2TwistTurns(session.getPlayer2TwistTurnsLeft());
            response.setTwistActive(session.isTwistActive());
            response.setLastMove(session.getLastMove());
            response.setLastMovePlayer(session.getLastMovePlayer());
            response.setBotError(session.getBotError());
            response.setBotThinking(session.isBotTurn() && session.getBotError() == null);
            return response;
        }
    }
}
