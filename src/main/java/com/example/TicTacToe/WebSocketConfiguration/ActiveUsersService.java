package com.example.TicTacToe.WebSocketConfiguration;

import com.example.TicTacToe.GlobalException.PlayerNotFoundException;
import com.example.TicTacToe.Models.Enums.PlayerStatus;
import com.example.TicTacToe.Models.Player;
import com.example.TicTacToe.Repositories.PlayerRepositories;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class ActiveUsersService {
    private final Map<String, String> userSessionMap  = new HashMap<>();

    @Autowired
    private PlayerRepositories playerRepositories;

    public void register(String username, String sessionId){
        userSessionMap.put(username, sessionId);
    }

    public void unregister(String sessionId){
        userSessionMap.values().removeIf(session -> session.equals(sessionId));
    }
    public boolean isUserOnline(String username){
        Player player = playerRepositories.findPlayerByUsername(username).orElseThrow(
                () -> new PlayerNotFoundException("Player not found")
        );
        boolean flag = player.getPlayerStatus().equals(PlayerStatus.PLAYING);
        return flag || userSessionMap.containsKey(username);
    }
}
