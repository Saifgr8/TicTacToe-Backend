package com.example.TicTacToe.Contollers.MatchDataController;

import com.example.TicTacToe.DTOs.DTOMatchData.DTOMatchMoveRequest;
import com.example.TicTacToe.Models.MatchData;
import com.example.TicTacToe.Services.MatchDataService.MatchDataService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/matchdata")
public class MatchDataController {

    private final MatchDataService matchDataService;

    public MatchDataController(MatchDataService matchDataService) {
        this.matchDataService = matchDataService;
    }

    @GetMapping("/getMoves")
    public ResponseEntity<List<MatchData>> fetchAllMatchMovesController(@RequestParam Long matchId){

        List<MatchData> data = matchDataService.fetchAllMovesService(matchId);

        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    @PostMapping("/saveMove")
    public ResponseEntity<MatchData> saveMoveInMatchDataController(@Valid @RequestBody DTOMatchMoveRequest request){

        MatchData data = matchDataService.saveMoveInMatchDataService(request);

        return new ResponseEntity<>(data, HttpStatus.CREATED);
    }
}
