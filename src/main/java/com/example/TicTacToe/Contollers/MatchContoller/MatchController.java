package com.example.TicTacToe.Contollers.MatchContoller;

import com.example.TicTacToe.DTOs.DTOMatch.DTOMatchEndRequest;
import com.example.TicTacToe.DTOs.GameDTOs.DTOGameCreate;
import com.example.TicTacToe.DTOs.GameDTOs.DTOGameMove;
import com.example.TicTacToe.DTOs.GameDTOs.DTOGameSessionResponse;
import com.example.TicTacToe.GAME.GameSession;
import com.example.TicTacToe.GlobalException.UnauthorizedRequestException;
import com.example.TicTacToe.Interfaces.ContollerInterfaces.MatchControllerInterface;
import com.example.TicTacToe.Models.Match;
import com.example.TicTacToe.Services.MatchService.MatchService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/match")
public class MatchController  implements MatchControllerInterface {

    public final MatchService matchService;

    public MatchController(MatchService matchService) {
        this.matchService = matchService;
    }

    @GetMapping("/player/{id}") //used for player dashboard
    public ResponseEntity<List<Match>> getAllMatchesPlayedByPlayer(@PathVariable Long id, Principal principal){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if(authentication == null || !authentication.isAuthenticated()){
            throw new UnauthorizedRequestException("Token failure, please log in again");
        }

        List<Match> matches = matchService.fetchAllMatchesForPlayer(id);

        return new ResponseEntity<>(matches, HttpStatus.OK);
    }

    @PostMapping("/create")
    public ResponseEntity<DTOGameSessionResponse> createMatch(@RequestBody DTOGameCreate request){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication == null || !authentication.isAuthenticated()){
            throw new UnauthorizedRequestException("Token failure, please log in again");
        }
        DTOGameSessionResponse response = matchService.createMatch(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/move")
    public ResponseEntity<DTOGameSessionResponse> makeMove(@RequestBody DTOGameMove request){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication == null || !authentication.isAuthenticated()){
            throw new UnauthorizedRequestException("Token failure, please log in again");
        }
        DTOGameSessionResponse response = matchService.makeMove(request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/session/{sessionId}")
    public ResponseEntity<DTOGameSessionResponse> getSessionState(@PathVariable String sessionId,
                                                                  Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedRequestException("Token failure, please log in again");
        }
        return ResponseEntity.ok(matchService.getSessionState(sessionId, authentication.getName()));
    }

    @PostMapping("/end")
    public ResponseEntity<Match> endMatch(@RequestBody DTOMatchEndRequest request){

        Match match = matchService.endMatchService(request);

        return new ResponseEntity<>(match,HttpStatus.OK);
    }
    @PostMapping("/disconnect")
    public ResponseEntity<Match> disconnectMatch(@RequestBody DTOMatchEndRequest request){

        Match match = matchService.disconnectMatchService(request);

        return new ResponseEntity<>(match,HttpStatus.OK);
    }

}
