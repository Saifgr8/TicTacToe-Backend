package com.example.TicTacToe.GAME.UTILS;

import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;

@Component
public class GetAllDiagonalPairsWithoutMain {

    public GetAllDiagonalPairsWithoutMain() {
    }

    public List<String> getAllDiagonalPairs(String[][] board) {
        int n = board.length;
        List<String> diagonal = new LinkedList<>();
        for (int i = 0; i <= n - 3; i++) {
            for (int j = 0; j <= n - 3; j++) {
                if (i == j) continue;

                String d1 = board[i][j];
                String d2 = board[i + 1][j + 1];
                String d3 = board[i + 2][j + 2];
                diagonal.add(i + "-" + j + "-" + (j + 1) + "-" + (j + 2));
            }
        }
        return diagonal;
    }


}
