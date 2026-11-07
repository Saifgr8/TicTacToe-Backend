package com.example.TicTacToe.DTOs.GameDTOs;

import com.example.TicTacToe.GAME.Enums.MoveType;
import com.example.TicTacToe.GAME.Enums.PatternType;
import jakarta.validation.constraints.NotNull;

public class DTOGameMove {

    @NotNull(message = "Game session ID is needed")
    private String sessionId;// ID of the ongoing game session
    @NotNull(message = "I coordinate is needed")
    private int x;                   // Row position
    @NotNull(message = "J coordinate is needed")
    private int y;                   // Column position
    @NotNull(message = "Playing symbol is needed")
    private String symbol;           // "X" or "O"
    @NotNull(message = "Move type is needed")
    private MoveType moveType;       // NORMAL, OVERRIDE, etc.
    private String[][] board;
    private PatternType patternType;

    public DTOGameMove() {
    }

    public DTOGameMove(String sessionId, int x, int y, String symbol, MoveType moveType, PatternType patternType) {
        this.sessionId = sessionId;
        this.x = x;
        this.y = y;
        this.symbol = symbol;
        this.moveType = moveType;
        this.patternType = patternType;
    }

    public String getSessionId() {
        return sessionId;
    }

    public PatternType getPatternType() {
        return patternType;
    }

    public void setPatternType(PatternType patternType) {
        this.patternType = patternType;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String[][] getBoard() {
        return board;
    }

    public void setBoard(String[][] board) {
        this.board = board;
    }

    public MoveType getMoveType() {
        return moveType;
    }

    public void setMoveType(MoveType moveType) {
        this.moveType = moveType;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }
}
