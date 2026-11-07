package com.example.TicTacToe.Interfaces.ServiceInterfaces;

import com.example.TicTacToe.DTOs.DTOMatch.DTOMatchEndRequest;
import com.example.TicTacToe.DTOs.GameDTOs.DTOGameCreate;
import com.example.TicTacToe.DTOs.GameDTOs.DTOGameSessionResponse;
import com.example.TicTacToe.Models.Match;

import java.util.List;

public interface MatchServiceInterface {
    List<Match> fetchAllMatchesForPlayer(Long id);
    DTOGameSessionResponse createMatch(DTOGameCreate request);
    Match endMatchService(DTOMatchEndRequest request);
    Match disconnectMatchService(DTOMatchEndRequest request);
}
