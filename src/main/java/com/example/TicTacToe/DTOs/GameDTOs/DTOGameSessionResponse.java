package com.example.TicTacToe.DTOs.GameDTOs;

import com.example.TicTacToe.GAME.Enums.GameStatus;
import jakarta.validation.constraints.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class DTOGameSessionResponse {

    private String sessionId;
    private String[][] board;
    private String currentPlayer;
    private int player1Points;
    private int player1TwistTurns;
    private String player1Symbol;
    private int player2Points;
    private int player2TwistTurns;
    private String player2Symbol;
    private GameStatus gameStatus;
    private boolean isTwistActive;
    private String winner;
    private int doubleOverrideTurnsLeft;
    private boolean isDoubleOverrideActive;
    private List<String> newPattern;
    private List<String> winPattern;
    private DTOGameMove lastMove;
    private String lastMovePlayer;
    private boolean botThinking;
    private String botError;


    public DTOGameSessionResponse() {
    }

    public DTOGameMove getLastMove() {
        return lastMove;
    }

    public void setLastMove(DTOGameMove lastMove) {
        this.lastMove = lastMove;
    }

    public String getLastMovePlayer() {
        return lastMovePlayer;
    }

    public void setLastMovePlayer(String lastMovePlayer) {
        this.lastMovePlayer = lastMovePlayer;
    }

    public boolean isBotThinking() {
        return botThinking;
    }

    public void setBotThinking(boolean botThinking) {
        this.botThinking = botThinking;
    }

    public String getBotError() {
        return botError;
    }

    public void setBotError(String botError) {
        this.botError = botError;
    }

    public DTOGameSessionResponse(String sessionId, boolean isTwistActive, String player2Symbol,
                                  int player2Points, String player1Symbol,
                                  int player1Points, String[][] board,
                                  String currentPlayer, int player1TwistTurns,
                                  int player2TwistTurns, GameStatus gameStatus, int doubleOverrideTurnsLeft, boolean isDoubleOverrideActive
    ,String winner, List<String> threePatterns,
                                  List<String> winPattern) {
        this.sessionId = sessionId;
        this.isTwistActive = isTwistActive;
        this.player2Symbol = player2Symbol;
        this.player2Points = player2Points;
        this.player1Symbol = player1Symbol;
        this.player1Points = player1Points;
        this.board = board;
        this.currentPlayer = currentPlayer;
        this.player1TwistTurns = player1TwistTurns;
        this.player2TwistTurns = player2TwistTurns;
        this.gameStatus = gameStatus;
        this.doubleOverrideTurnsLeft =doubleOverrideTurnsLeft;
        this.isDoubleOverrideActive = isDoubleOverrideActive;
        this.winner = winner;
        this.newPattern = threePatterns;
        this.winPattern = winPattern;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getWinner() {
        return winner;
    }

    public void setWinner(String winner) {
        this.winner = winner;
    }

    public List<String> getThreePatterns() {
        return newPattern;
    }

    public void setThreePatterns(List<String> threePatterns) {
        this.newPattern = threePatterns;
    }



    public boolean isDoubleOverrideActive() {
        return isDoubleOverrideActive;
    }

    public void setDoubleOverrideActive(boolean doubleOverrideActive) {
        isDoubleOverrideActive = doubleOverrideActive;
    }

    public int getDoubleOverrideTurnsLeft() {
        return doubleOverrideTurnsLeft;
    }

    public void setDoubleOverrideTurnsLeft(int doubleOverrideTurnsLeft) {
        this.doubleOverrideTurnsLeft = doubleOverrideTurnsLeft;
    }

    public String getPlayer2Symbol() {
        return player2Symbol;
    }

    public void setPlayer2Symbol(String player2Symbol) {
        this.player2Symbol = player2Symbol;
    }

    public int getPlayer2Points() {
        return player2Points;
    }

    public void setPlayer2Points(int player2Points) {
        this.player2Points = player2Points;
    }

    public int getPlayer1Points() {
        return player1Points;
    }

    public void setPlayer1Points(int player1Points) {
        this.player1Points = player1Points;
    }

    public String[][] getBoard() {
        return board;
    }

    public void setBoard(String[][] board) {
        this.board = board;
    }

    public String getCurrentPlayer() {
        return currentPlayer;
    }

    public void setCurrentPlayer(String currentPlayer) {
        this.currentPlayer = currentPlayer;
    }

    public int getPlayer1TwistTurns() {
        return player1TwistTurns;
    }

    public void setPlayer1TwistTurns(int player1TwistTurns) {
        this.player1TwistTurns = player1TwistTurns;
    }

    public String getPlayer1Symbol() {
        return player1Symbol;
    }

    public void setPlayer1Symbol(String player1Symbol) {
        this.player1Symbol = player1Symbol;
    }

    public int getPlayer2TwistTurns() {
        return player2TwistTurns;
    }

    public void setPlayer2TwistTurns(int player2TwistTurns) {
        this.player2TwistTurns = player2TwistTurns;
    }

    public boolean isTwistActive() {
        return isTwistActive;
    }

    public void setTwistActive(boolean twistActive) {
        isTwistActive = twistActive;
    }

    public GameStatus getGameStatus() {
        return gameStatus;
    }

    public void setGameStatus(GameStatus gameStatus) {
        this.gameStatus = gameStatus;
    }

    public List<String> getWinPattern() {
        return winPattern;
    }

    public void setWinPattern(List<String> winPattern) {
        this.winPattern = winPattern;
    }

    @Override
    public String toString() {
        return "DTOGameSessionResponse{" +
                "sessionId='" + sessionId + '\'' +
                ", board=" + Arrays.toString(board) +
                ", currentPlayer='" + currentPlayer + '\'' +
                ", player1Points=" + player1Points +
                ", player1TwistTurns=" + player1TwistTurns +
                ", player1Symbol='" + player1Symbol + '\'' +
                ", player2Points=" + player2Points +
                ", player2TwistTurns=" + player2TwistTurns +
                ", player2Symbol='" + player2Symbol + '\'' +
                ", gameStatus=" + gameStatus +
                ", isTwistActive=" + isTwistActive +
                ", winner='" + winner + '\'' +
                ", doubleOverrideTurnsLeft=" + doubleOverrideTurnsLeft +
                ", isDoubleOverrideActive=" + isDoubleOverrideActive +
                '}';
    }
}
