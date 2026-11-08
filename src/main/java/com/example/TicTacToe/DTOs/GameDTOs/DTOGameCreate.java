package com.example.TicTacToe.DTOs.GameDTOs;

import com.example.TicTacToe.GAME.Enums.GameModes;
import com.example.TicTacToe.GAME.GameEngine;
import com.example.TicTacToe.Models.Player;
import jakarta.validation.constraints.NotNull;

public class DTOGameCreate {

    @NotNull(message = "Board size is needed")
    private int boardSize;
    @NotNull(message = "Player1 username is needed")
    private String player1Username;
    @NotNull(message = "Player2 username is needed")
    private String player2Username;
    @NotNull(message = "Player 1 symbol is needed")
    private String player1Symbol;
    @NotNull(message = "Player 2 symbol is needed")
    private String player2Symbol;
    @NotNull(message = "Actual username is needed")
    private String startingPlayer;
    @NotNull(message = "Game mode is needed")
    private GameModes gameMode;

    public DTOGameCreate() {
    }

    public DTOGameCreate(int boardSize, String player2Symbol, String player1Symbol, String player2, String player1
            , String startingPlayer, GameModes gameModes) {
        this.boardSize = boardSize;
        this.player2Symbol = player2Symbol;
        this.player1Symbol = player1Symbol;
        this.player2Username = player2;
        this.player1Username = player1;
        this.startingPlayer = startingPlayer;
        this.gameMode = gameModes;
    }


    public int getBoardSize() {
        return boardSize;
    }

    public void setBoardSize(int boardSize) {
        this.boardSize = boardSize;
    }

    public String getPlayer2Symbol() {
        return player2Symbol;
    }

    public void setPlayer2Symbol(String player2Symbol) {
        this.player2Symbol = player2Symbol;
    }

    public String getPlayer1Symbol() {
        return player1Symbol;
    }

    public void setPlayer1Symbol(String player1Symbol) {
        this.player1Symbol = player1Symbol;
    }

    public String getPlayer1Username() {
        return player1Username;
    }

    public void setPlayer1Username(String player1Username) {
        this.player1Username = player1Username;
    }

    public String getStartingPlayer() {
        return startingPlayer;
    }

    public void setStartingPlayer(String startingPlayer) {
        this.startingPlayer = startingPlayer;
    }

    public String getPlayer2Username() {
        return player2Username;
    }

    public void setPlayer2Username(String player2Username) {
        this.player2Username = player2Username;
    }

    public GameModes getGameMode() {
        return gameMode;
    }

    public void setGameMode(GameModes gameMode) {
        this.gameMode = gameMode;
    }

    @Override
    public String toString() {
        return "DTOGameCreate{" +
                "boardSize=" + boardSize +
                ", player1Username='" + player1Username + '\'' +
                ", player2Username='" + player2Username + '\'' +
                ", player1Symbol='" + player1Symbol + '\'' +
                ", player2Symbol='" + player2Symbol + '\'' +
                '}';
    }
}
