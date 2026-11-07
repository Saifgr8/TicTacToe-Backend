package com.example.TicTacToe.GlobalException.GameExceptions;

public class MoveValidationFailException extends RuntimeException {
    public MoveValidationFailException(String message) {
        super(message);
    }
}
