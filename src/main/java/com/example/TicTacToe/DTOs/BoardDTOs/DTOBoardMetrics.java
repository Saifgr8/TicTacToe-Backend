package com.example.TicTacToe.DTOs.BoardDTOs;

public class DTOBoardMetrics {
    private int rowIndex;

    private int colIndex;

    private int leftDiagonalIndex;

    private int rightDiagonalIndex;

    private String rowComment;
    private String colComment;
    private String leftDiagonalComment;
    private String rightDiagonalComment;

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
