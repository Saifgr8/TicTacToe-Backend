package com.example.TicTacToe.GlobalException.GameExceptions;

public class InvalidSessionException extends RuntimeException {
    public InvalidSessionException(String message) {
        super(message);
    }
}
