package com.example.TicTacToe.GlobalException;

public class NoMatchDataFoundException extends RuntimeException {
    public NoMatchDataFoundException(String message) {
        super(message);
    }
}
