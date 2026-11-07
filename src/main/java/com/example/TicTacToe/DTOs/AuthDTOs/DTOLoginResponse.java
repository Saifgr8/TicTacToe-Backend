package com.example.TicTacToe.DTOs.AuthDTOs;

import com.example.TicTacToe.Models.Player;

public class DTOLoginResponse {
    private  String jwt;
    private  Player player;

    public DTOLoginResponse(String jwt, Player player) {
        this.jwt = jwt;
        this.player = player;
    }

    public String getJwt() {
        return jwt;
    }

    public void setJwt(String jwt) {
        this.jwt = jwt;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }
}
