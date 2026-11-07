package com.example.TicTacToe.GlobalException.GameExceptions;

public class InvalidTurnException extends RuntimeException {
    public InvalidTurnException(String message) {
        super(message);
    }
}
