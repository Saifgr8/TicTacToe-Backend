package com.example.TicTacToe.Services;


import com.example.TicTacToe.DTOs.AuthDTOs.DTOAuthLoginRequest;
import com.example.TicTacToe.DTOs.AuthDTOs.DTOAuthRegisterRequest;
import com.example.TicTacToe.DTOs.AuthDTOs.DTOLoginResponse;
import com.example.TicTacToe.GlobalException.PlayerAlreadyExistsException;
import com.example.TicTacToe.GlobalException.PlayerNotFoundException;
import com.example.TicTacToe.JWT.JWTUtils;
import com.example.TicTacToe.Models.Player;
import com.example.TicTacToe.Repositories.PlayerRepositories;
import com.example.TicTacToe.Services.AuthServices.PlayerAuthService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AuthService {
    @Mock
    private PlayerRepositories playerRepositories;

    @InjectMocks
    private PlayerAuthService playerAuthService;

    @Mock
    JWTUtils jwtUtils;

    @Mock
    PasswordEncoder passwordEncoder;
    @BeforeEach
    void setup(){
        MockitoAnnotations.openMocks(this);
    }
    @Test
    void testRegisterPlayer_shouldThrowException_whenEmailAlreadyExists(){
        DTOAuthRegisterRequest request = new DTOAuthRegisterRequest("test@gmail.com", "test101", "1234545");
        Player existingPlayer = new Player("Saifgr7@gmail.com", "saifgr8", "123445");

        when(playerRepositories.findPlayerByEmail("test@gmail.com")).thenReturn(Optional.of(existingPlayer));

        PlayerAlreadyExistsException ex = assertThrows(PlayerAlreadyExistsException.class,
                () -> playerAuthService.registerPlayerService(request));

        assertTrue(ex.getMessage().contains("Player Already Exists, Cannot register."));
    }
    @Test
    void testRegisterPlayer_shouldRegisterAndReturnPlayer(){
        DTOAuthRegisterRequest request = new DTOAuthRegisterRequest("saifgr7@gmail.com", "saif", "123456");

        when(playerRepositories.findPlayerByEmail(request.getEmail())).thenReturn(Optional.empty());

        Player newPlayer = new Player(request.getEmail(), request.getUsername(), request.getPassword());
        when(playerRepositories.save(any(Player.class))).thenReturn(newPlayer);

        Player result = playerAuthService.registerPlayerService(request);

        assertEquals(request.getEmail(), result.getEmail());
        assertEquals(request.getUsername(), result.getUsername());
    }
    @Test
    void testLoginPlayer_shouldThrowException_whenEmailNotExists(){
        DTOAuthLoginRequest request = new DTOAuthLoginRequest("test@gmail.com", "123456");
        Player existingPlayer = new Player("Saifgr7@gmail.com", "saifgr8", "123445");

        when(playerRepositories.findPlayerByEmail(request.getEmail())).thenReturn(Optional.empty());

        PlayerNotFoundException ex = assertThrows(PlayerNotFoundException.class,
                () -> playerAuthService.loginPlayerService(request));

        assertEquals("Player not found", ex.getMessage());
    }
    @Test
    void testPLayerLogin_shouldReturnJWT(){
        DTOAuthLoginRequest request = new DTOAuthLoginRequest("saif@gmail.com", "123456");
        Player existingPlayer = new Player("saif@gmail.com", "saifgr8123", "hashed_password");
        when(playerRepositories.findPlayerByEmail(request.getEmail())).thenReturn(Optional.of(existingPlayer));

        when(passwordEncoder.matches("123456","hashed_password")).thenReturn(true);

        when(jwtUtils.generateJWTFromEmail(any(UserDetails.class)))
                .thenReturn("dummy.jwt.token");

        DTOLoginResponse result = playerAuthService.loginPlayerService(request);
        assertNotNull(result.getJwt());
    }
    @Test
    void testGetPlayerByUsername_ShouldThrowException_InvalidUsernameProvided(){
        String username = "jazjaz";
        Player existingPlayer = new Player("Saifgr7@gmail.com", "saifgr8", "123456");
        when(playerRepositories.findPlayerByUsername(username)).thenReturn(Optional.empty());
        PlayerNotFoundException ex = assertThrows(PlayerNotFoundException.class,
                () -> playerAuthService.getPlayerByUsernameService(username));

        assertEquals("Player not found", ex.getMessage());

    }
    @Test
    void testGetPlayerByUsername_ShouldReturnPlayer(){
        String username = "saifgr8";
        Player existingPlayer = new Player("Saifgr7@gmail.com", "saifgr8", "123456");
        when(playerRepositories.findPlayerByUsername(username)).thenReturn(Optional.of(existingPlayer));
        Player result = playerAuthService.getPlayerByUsernameService(username);
        assertEquals(existingPlayer.getEmail(), result.getEmail());

    }

}
