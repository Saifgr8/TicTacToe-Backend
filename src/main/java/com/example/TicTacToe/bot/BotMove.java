package com.example.TicTacToe.bot;

import com.example.TicTacToe.GAME.Enums.MoveType;
import com.example.TicTacToe.GAME.Enums.PatternType;

public class BotMove {
    private int i;
    private int j;

    private MoveType moveType;
    private PatternType patternType;

    public BotMove() {
    }

    public BotMove(int i, PatternType patternType, MoveType moveType, int j) {
        this.i = i;
        this.patternType = patternType;
        this.moveType = moveType;
        this.j = j;
    }


    public int getI() {
        return i;
    }

    public void setI(int i) {
        this.i = i;
    }

    public MoveType getMoveType() {
        return moveType;
    }

    public void setMoveType(MoveType moveType) {
        this.moveType = moveType;
    }

    public int getJ() {
        return j;
    }

    public void setJ(int j) {
        this.j = j;
    }

    public PatternType getPatternType() {
        return patternType;
    }

    public void setPatternType(PatternType patternType) {
        this.patternType = patternType;
    }
}
