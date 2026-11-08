package com.example.TicTacToe.GAME;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;
import java.util.stream.Collectors;

public class TestTry {
    private static final Logger log = LoggerFactory.getLogger(TestTry.class);
    public static void main(String[] args) {
        String[][] arr = {{"0","1","2","3", "4"},{"0","1","2","3", "4"},{"0","1","2","3", "4"},{"0","1","2","3", "4"},{"0","1","2","3", "4"}};
        int n = arr.length;
        List<String> antiDiag = new LinkedList<>();

        for (int i = 0; i <= n - 3; i++) {
            for (int j = 2; j < n; j++) {
                String d1 = arr[i][j];
                String d2 = arr[i + 1][j - 1];
                String d3 = arr[i + 2][j - 2];
                antiDiag.add("A-" + i + "-" + j + (j - 1) + (j - 2));
            }
        }
        log.info("Anti-diagonal patterns: {}", antiDiag);
    }
}
