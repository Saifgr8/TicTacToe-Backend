package com.example.TicTacToe.DTOs.BoardDTOs;

import com.example.TicTacToe.GAME.Enums.PatternType;

public class DTOBoardMetrics {
    private int rowIndex;

    private int colIndex;

    private int leftDiagonalIndex;

    private int rightDiagonalIndex;

    private String rowComment;
    private String colComment;
    private String leftDiagonalComment;
    private String rightDiagonalComment;
    private PatternType criticalPattern;
    private int criticalRow;
    private int criticalColumn;
    private int criticalRemainingCells;

    public DTOBoardMetrics(int rightDiagonalIndex,
                           int colIndex, int rowIndex,
                           int leftDiagonalIndex, String rowComment, String colComment, String leftDiagonalComment, String rightDiagonalComment) {
        this.rightDiagonalIndex = rightDiagonalIndex;
        this.colIndex = colIndex;
        this.rowIndex = rowIndex;
        this.leftDiagonalIndex = leftDiagonalIndex;
        this.rowComment = rowComment;
        this.colComment = colComment;
        this.leftDiagonalComment = leftDiagonalComment;
        this.rightDiagonalComment = rightDiagonalComment;

    }



    public int getLeftDiagonalIndex() {
        return leftDiagonalIndex;
    }

    public void setLeftDiagonalIndex(int leftDiagonalIndex) {
        this.leftDiagonalIndex = leftDiagonalIndex;
    }

    public int getColIndex() {
        return colIndex;
    }

    public void setColIndex(int colIndex) {
        this.colIndex = colIndex;
    }

    public int getRowIndex() {
        return rowIndex;
    }

    public void setRowIndex(int rowIndex) {
        this.rowIndex = rowIndex;
    }



    public int getRightDiagonalIndex() {
        return rightDiagonalIndex;
    }

    public void setRightDiagonalIndex(int rightDiagonalIndex) {
        this.rightDiagonalIndex = rightDiagonalIndex;
    }

    public PatternType getCriticalPattern() {
        return criticalPattern;
    }

    public void setCriticalPattern(PatternType criticalPattern) {
        this.criticalPattern = criticalPattern;
    }

    public int getCriticalRow() {
        return criticalRow;
    }

    public void setCriticalRow(int criticalRow) {
        this.criticalRow = criticalRow;
    }

    public int getCriticalColumn() {
        return criticalColumn;
    }

    public void setCriticalColumn(int criticalColumn) {
        this.criticalColumn = criticalColumn;
    }

    public int getCriticalRemainingCells() {
        return criticalRemainingCells;
    }

    public void setCriticalRemainingCells(int criticalRemainingCells) {
        this.criticalRemainingCells = criticalRemainingCells;
    }

    public String getRowComment() {
        return rowComment;
    }

    public void setRowComment(String rowComment) {
        this.rowComment = rowComment;
    }

    public String getColComment() {
        return colComment;
    }

    public void setColComment(String colComment) {
        this.colComment = colComment;
    }

    public String getRightDiagonalComment() {
        return rightDiagonalComment;
    }

    public void setRightDiagonalComment(String rightDiagonalComment) {
        this.rightDiagonalComment = rightDiagonalComment;
    }

    public String getLeftDiagonalComment() {
        return leftDiagonalComment;
    }

    public void setLeftDiagonalComment(String leftDiagonalComment) {
        this.leftDiagonalComment = leftDiagonalComment;
    }
}
