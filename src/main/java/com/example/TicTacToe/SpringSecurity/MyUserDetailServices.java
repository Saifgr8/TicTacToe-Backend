package com.example.TicTacToe.SpringSecurity;

import com.example.TicTacToe.GlobalException.PlayerNotFoundException;
import com.example.TicTacToe.Models.Player;
import com.example.TicTacToe.Repositories.PlayerRepositories;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class MyUserDetailServices implements UserDetailsService {

    private final PlayerRepositories playerRepositories;

    public MyUserDetailServices(PlayerRepositories playerRepositories) {
        this.playerRepositories = playerRepositories;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<Player> temp = playerRepositories.findPlayerByEmail(username);
        if(temp.isEmpty()){
            throw new PlayerNotFoundException("Player not found");
        }
        Player player = temp.get();
        return org.springframework.security.core.userdetails.User
                .withUsername(player.getEmail()).password(player.getPassword())
                .authorities(new SimpleGrantedAuthority("Role_"+ player.getRole().name()))
                .build();
    }
}
