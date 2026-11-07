package com.example.TicTacToe.Interfaces.ContollerInterfaces;

import com.example.TicTacToe.DTOs.DTOMatch.DTOMatchEndRequest;
import com.example.TicTacToe.DTOs.GameDTOs.DTOGameCreate;
import com.example.TicTacToe.DTOs.GameDTOs.DTOGameSessionResponse;
import com.example.TicTacToe.Models.Match;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.List;

public interface MatchControllerInterface {
    ResponseEntity<List<Match>> getAllMatchesPlayedByPlayer (Long id, Principal principal);
    ResponseEntity<DTOGameSessionResponse> createMatch(DTOGameCreate request);
    ResponseEntity<Match> endMatch( DTOMatchEndRequest request);
    ResponseEntity<Match> disconnectMatch( DTOMatchEndRequest request);
}
