package com.example.TicTacToe.DTOs.DTOMatch;

import jakarta.validation.constraints.NotNull;

public class DTOMatchEndRequest {
    @NotNull
    private Long matchId;
    @NotNull
    private Long winnerId;
    @NotNull
    private Long looserId;

    public DTOMatchEndRequest() {
    }

    public Long getMatchId() {
        return matchId;
    }

    public void setMatchId(Long matchId) {
        this.matchId = matchId;
    }

    public Long getLooserId() {
        return looserId;
    }

    public void setLooserId(Long looserId) {
        this.looserId = looserId;
    }

    public Long getWinnerId() {
        return winnerId;
    }

    public void setWinnerId(Long winnerId) {
        this.winnerId = winnerId;
    }

    @Override
    public String toString() {
        return "DTOMatchEndRequest{" +
                "matchId=" + matchId +
                ", winnerId=" + winnerId +
                ", looserId=" + looserId +
                '}';
    }
}
