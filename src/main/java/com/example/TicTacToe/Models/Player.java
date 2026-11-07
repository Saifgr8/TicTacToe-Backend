package com.example.TicTacToe.Models;

import com.example.TicTacToe.Models.Enums.PlayerStatus;
import com.example.TicTacToe.Models.Enums.Role;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "players")
public class Player {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "username", unique = true)
    private String username;

    @Column(name = "password", unique = false)
    private String password;

    @Column(name = "image_url")
    private String imageUrl;

    private int gamesPlayed = 0;
    private int gamesWon = 0;
    private int gamesLost = 0;

    @Column(name = "rank")
    private int MMR = 1000;

    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    final Role role = Role.USER;

    @Enumerated(EnumType.STRING)
    private PlayerStatus playerStatus;

    private LocalDateTime createdOn;

    private int behaviourScore;

    @ElementCollection
    @CollectionTable(name = "player_friends" , joinColumns = @JoinColumn(name = "player_id"))
    @Column(name = "friend_id")
    private Set<Long> friendsIds = new HashSet<>();

    public Player() {
    }

    public Player(String email, String username, String password) {
        this.email = email;
        this.username = username;
        this.password = password;
        this.createdOn = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getBehaviourScore() {
        return behaviourScore;
    }

    public void setBehaviourScore(int behaviourScore) {
        this.behaviourScore = behaviourScore;
    }

    public LocalDateTime getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(LocalDateTime createdOn) {
        this.createdOn = createdOn;
    }

    public PlayerStatus getPlayerStatus() {
        return playerStatus;
    }

    public void setPlayerStatus(PlayerStatus playerStatus) {
        this.playerStatus = playerStatus;
    }

    public int getMMR() {
        return MMR;
    }

    public Set<Long> getFriendsIds() {
        return friendsIds;
    }

    public void setFriendsIds(Set<Long> friendsIds) {
        this.friendsIds = friendsIds;
    }

    public void setMMR(int MMR) {
        this.MMR = MMR;
    }

    public int getGamesWon() {
        return gamesWon;
    }

    public void setGamesWon(int gamesWon) {
        this.gamesWon = gamesWon;
    }

    public int getGamesPlayed() {
        return gamesPlayed;
    }

    public void setGamesPlayed(int gamesPlayed) {
        this.gamesPlayed = gamesPlayed;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public int getGamesLost() {
        return gamesLost;
    }

    public void setGamesLost(int gamesLost) {
        this.gamesLost = gamesLost;
    }

    public Role getRole() {
        return role;
    }
}
