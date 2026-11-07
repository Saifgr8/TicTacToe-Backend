package com.example.TicTacToe.Services.MatchDataService;

import com.example.TicTacToe.DTOs.DTOMatchData.DTOMatchMoveRequest;
import com.example.TicTacToe.GlobalException.InvalidMoveException;
import com.example.TicTacToe.GlobalException.NoMatchDataFoundException;
import com.example.TicTacToe.GlobalException.NoMatchFoundWithIdException;
import com.example.TicTacToe.GlobalException.PlayerNotFoundException;
import com.example.TicTacToe.Models.Match;
import com.example.TicTacToe.Models.MatchData;
import com.example.TicTacToe.Models.Player;
import com.example.TicTacToe.Repositories.MatchDataRepository;
import com.example.TicTacToe.Repositories.MatchRepository;
import com.example.TicTacToe.Repositories.PlayerRepositories;
import com.example.TicTacToe.Utils.JsonToString;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MatchDataService {
    private final MatchDataRepository matchDataRepository;
    private final MatchRepository matchRepository;
    private final PlayerRepositories playerRepositories;


    private final JsonToString jsonToString = new JsonToString();

    public MatchDataService(MatchDataRepository matchDataRepository, MatchRepository matchRepository, PlayerRepositories playerRepositories) {
        this.matchDataRepository = matchDataRepository;
        this.matchRepository = matchRepository;
        this.playerRepositories = playerRepositories;
    }

    public List<MatchData> fetchAllMovesService(Long matchId) {

        Match match = matchRepository.findById(matchId).orElseThrow(
                () -> new NoMatchFoundWithIdException("No Match found with this Id"));
        List<MatchData> data = matchDataRepository.findByMatchIdOrderByIdAsc(matchId)
                .orElseThrow(() -> new NoMatchDataFoundException("No Match data found for this match"));

        return data;

    }

    public MatchData saveMoveInMatchDataService(@Valid DTOMatchMoveRequest request) {
        Match match = matchRepository.findById(request.getMatch_id()).orElseThrow(
                () -> new NoMatchFoundWithIdException("No Match found with this Id"));
        Player player = playerRepositories.findById(request.getPlayerId()).orElseThrow(
                () -> new PlayerNotFoundException("Player not found with this Id")
        );
        if(request.getX() < 0 || request.getY() < 0) {
            throw new InvalidMoveException("Illegal move cord to play");
        }
        String[][] board = request.getBoard();
        if(!board[request.getX()][request.getY()].isEmpty()){
            throw new InvalidMoveException("Cell Already Occupied, please choose a different cell");
        }
        board[request.getX()][request.getY()] = request.getSymbol();
        String boardJson = jsonToString.convertJsonToString(board);
        MatchData matchData = new MatchData(match,player.getUsername(),request.getX(),
                request.getY(), request.getSymbol(), request.isSpecialMove());
        matchData.setBoardSnapshot(boardJson);

        return matchDataRepository.save(matchData);
    }
}
