package com.example.TicTacToe.DTOs.GameDTOs;

public class DTORPSChoice {

    private String rpsMatchId;
    private String username;
    private String choice;
    private String player1;
    private String player2;

    public DTORPSChoice() {
    }

    public DTORPSChoice(String choice, String username, String player1, String player2, String rpsMatchId) {
        this.choice = choice;
        this.username = username;
        this.player1 = player1;
        this.player2 = player2;
        this.rpsMatchId = rpsMatchId;
    }

    public String getUsername() {
        return username;
    }

    public String getRpsMatchId() {
        return rpsMatchId;
    }

    public void setRpsMatchId(String rpsMatchId) {
        this.rpsMatchId = rpsMatchId;
    }

    public String getPlayer1() {
        return player1;
    }

    public void setPlayer1(String player1) {
        this.player1 = player1;
    }

    public String getPlayer2() {
        return player2;
    }

    public void setPlayer2(String player2) {
        this.player2 = player2;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getChoice() {
        return choice;
    }

    public void setChoice(String choice) {
        this.choice = choice;
    }

    @Override
    public String toString() {
        return "DTORPSChoice{" +
                "rpsMatchId='" + rpsMatchId + '\'' +
                ", username='" + username + '\'' +
                ", choice='" + choice + '\'' +
                ", player1='" + player1 + '\'' +
                ", player2='" + player2 + '\'' +
                '}';
    }
}
