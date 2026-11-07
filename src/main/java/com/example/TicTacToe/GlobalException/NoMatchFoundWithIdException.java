package com.example.TicTacToe.GlobalException;

public class NoMatchFoundWithIdException extends RuntimeException {
    public NoMatchFoundWithIdException(String message) {
        super(message);
    }
}
