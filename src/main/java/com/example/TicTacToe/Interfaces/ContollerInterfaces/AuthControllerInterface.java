package com.example.TicTacToe.Interfaces.ContollerInterfaces;

import com.example.TicTacToe.DTOs.AuthDTOs.DTOAuthLoginRequest;
import com.example.TicTacToe.DTOs.AuthDTOs.DTOAuthRegisterRequest;
import com.example.TicTacToe.Models.Player;
import org.springframework.http.ResponseEntity;

import java.util.Map;

public interface AuthControllerInterface {
    ResponseEntity<Player> registerPlayerController(DTOAuthRegisterRequest request);
    ResponseEntity<Player> loginPLayerController(DTOAuthLoginRequest request);
    ResponseEntity<Player> getPlayerByUsernameController(String username);
}
