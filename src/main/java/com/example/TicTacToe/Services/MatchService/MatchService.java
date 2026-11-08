package com.example.TicTacToe.Services.MatchService;

import com.example.TicTacToe.DTOs.DTOMatch.DTOMatchEndRequest;
import com.example.TicTacToe.DTOs.GameDTOs.DTOGameCreate;
import com.example.TicTacToe.DTOs.GameDTOs.DTOGameMove;
import com.example.TicTacToe.DTOs.GameDTOs.DTOGameSessionResponse;
import com.example.TicTacToe.GAME.Enums.MoveType;
import com.example.TicTacToe.GAME.Enums.PatternType;
import com.example.TicTacToe.GAME.GameSession;
import com.example.TicTacToe.GAME.GameSessionSnapshot;
import com.example.TicTacToe.GlobalException.GameExceptions.InvalidSessionException;
import com.example.TicTacToe.GlobalException.GameExceptions.MoveValidationFailException;
import com.example.TicTacToe.GlobalException.NoMatchFoundWithIdException;
import com.example.TicTacToe.GlobalException.NoMatchesForUsernameFoundException;
import com.example.TicTacToe.GlobalException.PlayerNotFoundException;
import com.example.TicTacToe.GlobalException.UnauthorizedRequestException;
import com.example.TicTacToe.Interfaces.ServiceInterfaces.MatchServiceInterface;
import com.example.TicTacToe.Models.Enums.MatchStatus;
import com.example.TicTacToe.Models.Enums.PlayerStatus;
import com.example.TicTacToe.Models.Match;
import com.example.TicTacToe.Models.Player;
import com.example.TicTacToe.Repositories.MatchRepository;
import com.example.TicTacToe.Repositories.PlayerRepositories;
import com.example.TicTacToe.WebSocketConfiguration.GameSessionManager;
import com.example.TicTacToe.bot.BotTurnScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MatchService implements MatchServiceInterface {

    private final MatchRepository matchRepository;
    private final PlayerRepositories playerRepositories;
    private final GameSessionManager sessionManager;
    private final BotTurnScheduler botTurnScheduler;

    public MatchService(MatchRepository matchRepository, PlayerRepositories playerRepositories,
                        GameSessionManager manager, BotTurnScheduler botTurnScheduler) {
        this.matchRepository = matchRepository;
        this.playerRepositories = playerRepositories;
        this.sessionManager = manager;
        this.botTurnScheduler = botTurnScheduler;
    }

    public List<Match> fetchAllMatchesForPlayer(Long id) {
        List<Match> matches = matchRepository.findAllByPlayer1_idOrPlayer2_id(id, id)
                .orElseThrow(() -> new NoMatchesForUsernameFoundException("No matches found for this user"));

        return matches;
    }

    @Transactional
    public DTOGameSessionResponse createMatch(DTOGameCreate request) {
        GameSession session = sessionManager.createSession(request);
        DTOGameSessionResponse response = GameSessionSnapshot.from(session);
        botTurnScheduler.scheduleAfterCommit(session);
        return response;
    }

    public DTOGameSessionResponse makeMove(DTOGameMove request) {
        GameSession session = sessionManager.getSession(request.getSessionId());

        if (session == null) {
            throw new InvalidSessionException("Unable to find session.");
        }
        synchronized (session) {
            boolean success = session.processMove(
                    request.getX(),
                    request.getY(),
                    request.getSymbol(),
                    request.getMoveType() != null ? request.getMoveType() : MoveType.NORMAL,
                    request.getPatternType() != null ? request.getPatternType() : PatternType.NONE
            );
            if (!success) {
                throw new MoveValidationFailException("Invalid Move");
            }
            DTOGameSessionResponse response = GameSessionSnapshot.from(session);
            botTurnScheduler.scheduleAfterCommit(session);
            return response;
        }
    }

    public DTOGameSessionResponse getSessionState(String sessionId, String email) {
        GameSession session = sessionManager.getSession(sessionId);
        if (session == null) {
            throw new InvalidSessionException("Unable to find session.");
        }
        Match match = session.getMatch();
        if (!email.equals(match.getPlayer1().getEmail()) && !email.equals(match.getPlayer2().getEmail())) {
            throw new UnauthorizedRequestException("You are not a player in this session.");
        }
        return GameSessionSnapshot.from(session);
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

}
