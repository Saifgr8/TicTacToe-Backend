package com.example.TicTacToe.DTOs.DTOMatchData;

import com.example.TicTacToe.Models.Player;
import jakarta.validation.constraints.NotNull;

public class DTOMatchMoveRequest {
    @NotNull(message = "Match Id is required")
    private Long match_id;

    @NotNull(message = "Player making move is required")
    private Long playerId;

    String[][] board;

    @NotNull(message = "x cord is needed")
    private int x;

    @NotNull(message = "y cord is needed")
    private int y;

    @NotNull(message = "Player symbol is needed")
    private String symbol;

    @NotNull(message = "Special move info is needed")
    private boolean specialMove;

    public DTOMatchMoveRequest(Long id, Long playerId, String[][] board, int x, int y, String symbol, boolean specialMove) {
        this.match_id = id;
        this.playerId = playerId;
        this.board = board;
        this.x = x;
        this.y = y;
        this.symbol = symbol;
        this.specialMove = specialMove;
    } public String[][] getBoard(){
        return board;
    }
    public void setBoard(String[][] board){
        this.board = board;
    }

    public Long getMatch_id() {
        return match_id;
    }

    public void setMatch_id(Long id) {
        this.match_id = id;
    }

    public boolean isSpecialMove() {
        return specialMove;
    }

    public void setSpecialMove(boolean specialMove) {
        this.specialMove = specialMove;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public Long getPlayerId() {
        return playerId;
    }

    public void setPlayerId(Long playerId) {
        this.playerId = playerId;
    }



    @Override
    public String toString() {
        return "DTOMatchMoveRequest{" +
                "id=" + match_id +
                ", player=" + playerId +
                ", x=" + x +
                ", y=" + y +
                ", symbol='" + symbol + '\'' +
                ", specialMove=" + specialMove +
                '}';
    }
}
