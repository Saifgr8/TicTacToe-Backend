package com.example.TicTacToe.DTOs.GameDTOs;

import com.example.TicTacToe.GAME.Enums.InviteStatus;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public class DTOGameInvite {
    @NotNull(message = "From invitation is required")
    private String from;
    @NotNull(message = "To invitation is required")
    private String to;

    private InviteStatus inviteStatus;

    private LocalDateTime invitedOn;

    public DTOGameInvite() {
    }

    public DTOGameInvite(String from, String to) {
        this.from = from;
        this.inviteStatus = InviteStatus.PENDING;
        this.to = to;
        this.invitedOn = LocalDateTime.now();
    }

    public String getFrom() {
        return from;
    }

    public InviteStatus getInviteStatus() {
        return inviteStatus;
    }

    public void setInviteStatus(InviteStatus inviteStatus) {
        this.inviteStatus = inviteStatus;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public LocalDateTime getInvitedOn() {
        return invitedOn;
    }

    public void setInvitedOn(LocalDateTime invitedOn) {
        this.invitedOn = invitedOn;
    }



    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    @Override
    public String toString() {
        return "DTOGameInvite{" +
                "from='" + from + '\'' +
                ", to='" + to + '\'' +
                ", invitedOn=" + invitedOn +
                '}';
    }
}
