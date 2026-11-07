package com.example.TicTacToe.DTOs.AuthDTOs;

public class DTOGoogleRequest {

    private String googleAccessToken;

    public DTOGoogleRequest() {
    }

    public DTOGoogleRequest(String googleAccessToken) {
        this.googleAccessToken = googleAccessToken;
    }

    public String getGoogleAccessToken() {
        return googleAccessToken;
    }

    public void setGoogleAccessToken(String googleAccessToken) {
        this.googleAccessToken = googleAccessToken;
    }
}
