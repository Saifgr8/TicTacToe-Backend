package com.example.TicTacToe.Services.AuthServices;

import com.example.TicTacToe.DTOs.AuthDTOs.DTOAuthLoginRequest;
import com.example.TicTacToe.DTOs.AuthDTOs.DTOAuthRegisterRequest;
import com.example.TicTacToe.DTOs.AuthDTOs.DTOGoogleRequest;
import com.example.TicTacToe.DTOs.AuthDTOs.DTOLoginResponse;
import com.example.TicTacToe.GlobalException.InvalidCredentialsException;
import com.example.TicTacToe.GlobalException.PlayerAlreadyExistsException;
import com.example.TicTacToe.GlobalException.PlayerNotFoundException;
import com.example.TicTacToe.Interfaces.ServiceInterfaces.AuthServicesInterfaces;
import com.example.TicTacToe.JWT.JWTUtils;
import com.example.TicTacToe.Models.Enums.PlayerStatus;
import com.example.TicTacToe.Models.Player;
import com.example.TicTacToe.Repositories.PlayerRepositories;
import com.example.TicTacToe.SpringSecurity.MyUserDetailServices;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


@Service
public class PlayerAuthService implements AuthServicesInterfaces {

    private final PlayerRepositories playerRepositories;
    private final PasswordEncoder passwordEncoder;
    private final JWTUtils jwtUtils;
    private final GoogleOAuthService googleOAuthService;
    private final MyUserDetailServices myUserDetailServices;

    public PlayerAuthService(PlayerRepositories playerRepositories,
                             PasswordEncoder passwordEncoder, JWTUtils jwtUtils,
                             GoogleOAuthService googleOAuthService,
                             MyUserDetailServices myUserDetailServices
    ) {
        this.playerRepositories = playerRepositories;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
        this.googleOAuthService = googleOAuthService;
        this.myUserDetailServices = myUserDetailServices;
    }

    @Override
    public Player registerPlayerService(DTOAuthRegisterRequest requestDTO) {
        Optional<Player> player = playerRepositories.findPlayerByEmail(requestDTO.getEmail());

        if (player.isPresent()) {
            throw new PlayerAlreadyExistsException("Player Already Exists, Cannot register.");
        }
        Player newPlayer = new Player();
        newPlayer.setEmail(requestDTO.getEmail());
        newPlayer.setUsername(requestDTO.getUsername());
        newPlayer.setPassword(passwordEncoder.encode(requestDTO.getPassword()));
        newPlayer.setPlayerStatus(PlayerStatus.OFFLINE);
        newPlayer.setCreatedOn(LocalDateTime.now());
        newPlayer.setGamesLost(0);
        newPlayer.setGamesWon(0);
        newPlayer.setGamesPlayed(0);
        newPlayer.setImageUrl("");
        newPlayer.setMMR(1000);
        return playerRepositories.save(newPlayer);
    }

    @Override
    public DTOLoginResponse loginPlayerService(DTOAuthLoginRequest requestDTO) {
        Player player = playerRepositories.findPlayerByEmail(requestDTO.getEmail())
                .orElseThrow(() -> new PlayerNotFoundException("Player not Found"));
        if (!passwordEncoder.matches(requestDTO.getPassword(), player.getPassword())) {
            throw new InvalidCredentialsException("Invalid credentials");
        }
        //CREATE USERDETAILS
        UserDetails userDetails = User
                .withUsername(player.getEmail())
                .password(player.getPassword())
                .authorities("ROLE_" + player.getRole().name())
                .build();
        //CREATE JWT
        String token = jwtUtils.generateJWTFromEmail(userDetails);

        DTOLoginResponse response = new DTOLoginResponse(token, player);
        return response;
    }

    @Override
    public Player getPlayerByUsernameService(String name) {
        return playerRepositories.findPlayerByUsername(name).orElseThrow(
                () -> new PlayerNotFoundException("Player not found"));
    }

    @Override
    public DTOLoginResponse loginViaGoogle(DTOGoogleRequest request) {
        //FETCH GOOGLE USER INFO
        GoogleOAuthService.GoogleUserInfo userInfo =
                googleOAuthService.verifyGoogleAccessTokenAndGetUserInfo(request);

        //FIND AND CREATE USER
        Player player = playerRepositories.findPlayerByEmail(userInfo.getEmail())
                .orElseGet(() -> {
                    Player newPlayer = new Player(userInfo.getEmail(), userInfo.getName(), null);
                    newPlayer.setPassword(passwordEncoder.encode("GOOGLE_AUTH_DUMMY_PASSWORD"));
                    newPlayer.setImageUrl(userInfo.getPicture());
                    newPlayer.setPlayerStatus(PlayerStatus.ONLINE);
                    return playerRepositories.save(newPlayer);
                });

        //CREATE A JWT
        UserDetails userDetails = myUserDetailServices.loadUserByUsername(player.getEmail());
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(auth);
        String jwt = jwtUtils.generateJWTFromEmail(userDetails);
        DTOLoginResponse response = new DTOLoginResponse(jwt, player);
        return response;
    }

}
