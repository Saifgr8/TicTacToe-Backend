package com.example.TicTacToe.Services.MatchService;

import com.example.TicTacToe.DTOs.DTOMatch.DTOMatchEndRequest;
import com.example.TicTacToe.DTOs.GameDTOs.DTOGameCreate;
import com.example.TicTacToe.DTOs.GameDTOs.DTOGameMove;
import com.example.TicTacToe.DTOs.GameDTOs.DTOGameSessionResponse;
import com.example.TicTacToe.GAME.Enums.MoveType;
import com.example.TicTacToe.GAME.Enums.PatternType;
import com.example.TicTacToe.GAME.GameSession;
import com.example.TicTacToe.GlobalException.GameExceptions.InvalidSessionException;
import com.example.TicTacToe.GlobalException.GameExceptions.MoveValidationFailException;
import com.example.TicTacToe.GlobalException.NoMatchFoundWithIdException;
import com.example.TicTacToe.GlobalException.NoMatchesForUsernameFoundException;
import com.example.TicTacToe.GlobalException.PlayerNotFoundException;
import com.example.TicTacToe.Interfaces.ServiceInterfaces.MatchServiceInterface;
import com.example.TicTacToe.Models.Enums.MatchStatus;
import com.example.TicTacToe.Models.Enums.PlayerStatus;
import com.example.TicTacToe.Models.Match;
import com.example.TicTacToe.Models.Player;
import com.example.TicTacToe.Repositories.MatchRepository;
import com.example.TicTacToe.Repositories.PlayerRepositories;
import com.example.TicTacToe.WebSocketConfiguration.GameSessionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MatchService implements MatchServiceInterface {

    private final MatchRepository matchRepository;
    private final PlayerRepositories playerRepositories;
    private final GameSessionManager sessionManager;

    public MatchService(MatchRepository matchRepository, PlayerRepositories playerRepositories, GameSessionManager manager) {
        this.matchRepository = matchRepository;
        this.playerRepositories = playerRepositories;
        this.sessionManager = manager;
    }

    public List<Match> fetchAllMatchesForPlayer(Long id) {
        List<Match> matches = matchRepository.findAllByPlayer1_idOrPlayer2_id(id, id)
                .orElseThrow(() -> new NoMatchesForUsernameFoundException("No matches found for this user"));

        return matches;
    }

    @Transactional
    public DTOGameSessionResponse createMatch(DTOGameCreate request) {
        System.out.println(request);
        GameSession session = sessionManager.createSession(request);
        DTOGameSessionResponse response = convertToDTO(session);
        return response;
    }

    public DTOGameSessionResponse makeMove(DTOGameMove request) {
        GameSession session = sessionManager.getSession(request.getSessionId());

        if (session == null) {
            throw new InvalidSessionException("Unable to find session.");
        }
        boolean success = session.processMove(
                request.getX(),
                request.getY(),
                request.getSymbol(),
                request.getMoveType() != null ? request.getMoveType() : MoveType.NORMAL,
                request.getPatternType() != null ? request.getPatternType() : PatternType.NONE
        );
        if (success) {
            DTOGameSessionResponse response = convertToDTO(session);
            return  response;
        } else{
            throw new MoveValidationFailException("Invalid Move");
        }
    }

    @Transactional
    public Match endMatchService(DTOMatchEndRequest request) {
        Match match = matchRepository.findById(request.getMatchId())
                .orElseThrow(() -> new NoMatchFoundWithIdException("No match found with this match id"));
        Player winnerPlayer = playerRepositories.findById(request.getWinnerId())
                .orElseThrow(() -> new PlayerNotFoundException("No player with this ID can be found"));
        Player looserPlayer = playerRepositories.findById(request.getLooserId())
                .orElseThrow(() -> new PlayerNotFoundException("No player with this ID can be found"));

        // Ensure winner & loser are actually part of this match
        if (!(match.getPlayer1().getId().equals(winnerPlayer.getId()) ||
                match.getPlayer2().getId().equals(winnerPlayer.getId())) ||
                !(match.getPlayer1().getId().equals(looserPlayer.getId()) ||
                        match.getPlayer2().getId().equals(looserPlayer.getId()))) {
            throw new RuntimeException("Winner or loser not part of this match");
        }

        //changes to match
        match.setEndedAt(LocalDateTime.now());
        match.setWinner(winnerPlayer.getEmail());
        match.setStatus(MatchStatus.FINISHED);

        //changes to player
        winnerPlayer.setGamesPlayed(winnerPlayer.getGamesPlayed() + 1);
        winnerPlayer.setGamesWon(winnerPlayer.getGamesWon() + 1);
        winnerPlayer.setMMR(Math.max(0, winnerPlayer.getMMR() + 25));
        winnerPlayer.setBehaviourScore(Math.max(100, winnerPlayer.getBehaviourScore() + 5));
        winnerPlayer.setPlayerStatus(PlayerStatus.ONLINE);

        looserPlayer.setGamesPlayed(looserPlayer.getGamesPlayed() + 1);
        looserPlayer.setGamesLost(looserPlayer.getGamesLost() + 1);
        looserPlayer.setMMR(Math.max(0, looserPlayer.getMMR() - 25));
        looserPlayer.setBehaviourScore(Math.max(100, looserPlayer.getBehaviourScore() + 5));
        looserPlayer.setPlayerStatus(PlayerStatus.ONLINE);

        playerRepositories.save(winnerPlayer);
        playerRepositories.save(looserPlayer);

        return matchRepository.save(match);
    }

    @Transactional
    public Match disconnectMatchService(DTOMatchEndRequest request) {
        Match match = matchRepository.findById(request.getMatchId())
                .orElseThrow(() -> new NoMatchFoundWithIdException("No match found with this match id"));
        Player winnerPlayer = playerRepositories.findById(request.getWinnerId())
                .orElseThrow(() -> new PlayerNotFoundException("No player with this ID can be found"));
        Player looserPlayer = playerRepositories.findById(request.getLooserId())
                .orElseThrow(() -> new PlayerNotFoundException("No player with this ID can be found"));

        // Ensure winner & loser are actually part of this match
        if (!(match.getPlayer1().getId().equals(winnerPlayer.getId()) ||
                match.getPlayer2().getId().equals(winnerPlayer.getId())) ||
                !(match.getPlayer1().getId().equals(looserPlayer.getId()) ||
                        match.getPlayer2().getId().equals(looserPlayer.getId()))) {
            throw new RuntimeException("Winner or loser not part of this match");
        }

        //changes to match
        match.setEndedAt(LocalDateTime.now());
        match.setWinner(winnerPlayer.getEmail());
        match.setStatus(MatchStatus.FINISHED);

        //changes to player
        winnerPlayer.setGamesPlayed(winnerPlayer.getGamesPlayed() + 1);
        winnerPlayer.setGamesWon(winnerPlayer.getGamesWon() + 1);
        winnerPlayer.setMMR(Math.max(0, winnerPlayer.getMMR() + 25));
        winnerPlayer.setBehaviourScore(Math.max(100, winnerPlayer.getBehaviourScore() + 5));
        winnerPlayer.setPlayerStatus(PlayerStatus.ONLINE);

        looserPlayer.setGamesPlayed(looserPlayer.getGamesPlayed() + 1);
        looserPlayer.setGamesLost(looserPlayer.getGamesLost() + 1);
        looserPlayer.setMMR(Math.max(0, looserPlayer.getMMR() - 35));
        looserPlayer.setBehaviourScore(Math.max(0, looserPlayer.getBehaviourScore() - 10));
        looserPlayer.setPlayerStatus(PlayerStatus.ONLINE);

        playerRepositories.save(winnerPlayer);
        playerRepositories.save(looserPlayer);

        return matchRepository.save(match);
    }

    private static DTOGameSessionResponse convertToDTO(GameSession session) {
        DTOGameSessionResponse response = new DTOGameSessionResponse();
        //GAME REALTED
        response.setSessionId(session.getSessionId());
        response.setBoard(session.getBoard());
        response.setGameStatus(session.getGameStatus());
        response.setCurrentPlayer(session.getCurrentPlayer());
        response.setWinner(session.getWinner());
        response.setDoubleOverrideActive(session.isDoubleOverride());
        response.setDoubleOverrideTurnsLeft(session.getDoubleOverrideTurns());
        response.setThreePatterns(session.getNewPattern());
        response.setWinPattern(session.getWinPattern());

        //PLAYER REALTED
        response.setPlayer1Points(session.getPlayer1Points());
        response.setPlayer1Symbol(session.getPlayer1Symbol());
        response.setPlayer1TwistTurns(session.getPlayer1TwistTurnsLeft());
        response.setPlayer2Points(session.getPlayer2Points());
        response.setPlayer2Symbol(session.getPlayer2Symbol());
        response.setPlayer2TwistTurns(session.getPlayer2TwistTurnsLeft());
        response.setTwistActive(session.isTwistActive());
        return response;
    }


}
