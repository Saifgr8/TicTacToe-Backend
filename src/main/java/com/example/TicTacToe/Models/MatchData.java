package com.example.TicTacToe.Models;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "match_moves")
public class MatchData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "match_id", nullable = false)
    private Match match;

    @Column(name = "player_username", nullable = false)
    private String player;

    @Column(name = "board_snapshot", columnDefinition = "TEXT")
    private String boardSnapshot;

    private int x;
    private int y;

    private String symbol;

    private LocalDateTime timestamp;

    private boolean specialMove;


    public MatchData(Match match, String  player, int x, int y, String symbol
    ,boolean specialMove) {
        this.match = match;
        this.player = player;
        this.x = x;
        this.y = y;
        this.symbol =symbol;
        this.specialMove = specialMove;
        this.timestamp = LocalDateTime.now();
    }

    public MatchData() {
    }

    public Long getId() {
        return id;
    }

    public String getBoardSnapshot() {
        return boardSnapshot;
    }

    public void setBoardSnapshot(String boardSnapshot) {
        this.boardSnapshot = boardSnapshot;
    }

    public boolean isSpecialMove() {
        return specialMove;
    }

    public void setSpecialMove(boolean specialMove) {
        this.specialMove = specialMove;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public String getPlayer() {
        return player;
    }

    public void setPlayer(String player) {
        this.player = player;
    }

    public Match getMatch() {
        return match;
    }

    public void setMatch(Match match) {
        this.match = match;
    }
}
