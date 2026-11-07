package com.example.TicTacToe.GAME.UTILS;

import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;

@Component
public class GetAllAntiDiagonalPairs {

    public GetAllAntiDiagonalPairs() {
    }

    public List<String> getAllAntiDiagPatterns(String[][] board) {
        int n = board.length;
        List<String> antiDiag = new LinkedList<>();

        for (int i = 0; i <= n - 3; i++) {
            for (int j = 2; j < n; j++) {
                // Create encoded pattern (optional values)
                String d1 = board[i][j];
                String d2 = board[i + 1][j - 1];
                String d3 = board[i + 2][j - 2];

                antiDiag.add( i + "-" + j + "-" +  (j - 1) + "-" +  (j - 2));
            }
        }
        return antiDiag;
    }

}
