package com.example.TicTacToe.GlobalException;

public class NoMatchesForUsernameFoundException extends RuntimeException {
    public NoMatchesForUsernameFoundException(String message) {
        super(message);
    }
}
