package com.example.TicTacToe.DTOs.GameDTOs;

public class DTORPSResult {

    private String matchId;

    private String player1Username;

    private String player2Username;

    private String player1Choice;

    private String player2Choice;

    private String winnerUsername;

    private String result;

    public DTORPSResult() {
    }

    public DTORPSResult(String player1Username, String player2Username, String player1Choice, String player2Choice, String winnerUsername, String result, String matchId) {
        this.player1Username = player1Username;
        this.player2Username = player2Username;
        this.player1Choice = player1Choice;
        this.player2Choice = player2Choice;
        this.winnerUsername = winnerUsername;
        this.result = result;
        this.matchId =matchId;
    }

    public String getPlayer1Username() {
        return player1Username;
    }

    public void setPlayer1Username(String player1Username) {
        this.player1Username = player1Username;
    }

    public String getPlayer2Username() {
        return player2Username;
    }

    public void setPlayer2Username(String player2Username) {
        this.player2Username = player2Username;
    }

    public String getPlayer1Choice() {
        return player1Choice;
    }

    public String getMatchId() {
        return matchId;
    }

    public void setMatchId(String matchId) {
        this.matchId = matchId;
    }

    public void setPlayer1Choice(String player1Choice) {
        this.player1Choice = player1Choice;
    }

    public String getPlayer2Choice() {
        return player2Choice;
    }

    public void setPlayer2Choice(String player2Choice) {
        this.player2Choice = player2Choice;
    }

    public String getWinnerUsername() {
        return winnerUsername;
    }

    public void setWinnerUsername(String winnerUsername) {
        this.winnerUsername = winnerUsername;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }
}
