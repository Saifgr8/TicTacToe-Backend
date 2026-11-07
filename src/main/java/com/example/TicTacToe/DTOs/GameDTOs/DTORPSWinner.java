package com.example.TicTacToe.DTOs.GameDTOs;

public class DTORPSWinner {
    private String winnerSymbol;
    private String looserPlayer;
    private String winnerPlayer;

    public DTORPSWinner(String winnerSymbol, String looserPlayer, String winnerPlayer) {
        this.winnerSymbol = winnerSymbol;
        this.looserPlayer = looserPlayer;
        this.winnerPlayer = winnerPlayer;
    }

    public DTORPSWinner() {
    }

    public String getWinnerSymbol() {
        return winnerSymbol;
    }

    public String getWinnerPlayer() {
        return winnerPlayer;
    }

    public void setWinnerPlayer(String winnerPlayer) {
        this.winnerPlayer = winnerPlayer;
    }

    public void setWinnerSymbol(String winnerSymbol) {
        this.winnerSymbol = winnerSymbol;
    }

    public String getLooserPlayer() {
        return looserPlayer;
    }

    public void setLooserPlayer(String looserPlayer) {
        this.looserPlayer = looserPlayer;
    }
}
