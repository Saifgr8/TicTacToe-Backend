package com.example.TicTacToe.Utils;

import org.springframework.stereotype.Component;

@Component
public class RPSWinner {

    public RPSWinner() {
    }

    public String determineRPSWinner(String player1Choice, String player2Choice, String player1Username, String player2Username) {

        if (player1Choice.equals(player2Choice)) return "DRAW";
        if ((player1Choice.equals("ROCK") && player2Choice.equals("SCISSORS")) ||
                (player1Choice.equals("PAPER") && player2Choice.equals("ROCK")) ||
                (player1Choice.equals("SCISSORS") && player2Choice.equals("PAPER"))) {
            return player1Username;
        } else {
            return player2Username;
        }
    }
}
