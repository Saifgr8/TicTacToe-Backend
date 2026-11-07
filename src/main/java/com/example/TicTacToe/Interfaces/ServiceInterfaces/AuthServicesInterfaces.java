package com.example.TicTacToe.Interfaces.ServiceInterfaces;

import com.example.TicTacToe.DTOs.AuthDTOs.DTOAuthLoginRequest;
import com.example.TicTacToe.DTOs.AuthDTOs.DTOAuthRegisterRequest;
import com.example.TicTacToe.DTOs.AuthDTOs.DTOGoogleRequest;
import com.example.TicTacToe.DTOs.AuthDTOs.DTOLoginResponse;
import com.example.TicTacToe.Models.Player;

import java.util.Map;

public interface AuthServicesInterfaces {
    Player registerPlayerService(DTOAuthRegisterRequest requestDTO);
    DTOLoginResponse loginPlayerService(DTOAuthLoginRequest requestDTO);

    Player getPlayerByUsernameService(String name);

    DTOLoginResponse loginViaGoogle(DTOGoogleRequest request);
}
