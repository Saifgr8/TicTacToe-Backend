package com.example.TicTacToe.GlobalException.GameExceptions;

public class SessionCreationException extends RuntimeException {
    public SessionCreationException(String message) {
        super(message);
    }
}
