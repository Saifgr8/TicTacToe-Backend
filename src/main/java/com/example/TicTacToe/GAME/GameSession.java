package com.example.TicTacToe.GAME;


import com.example.TicTacToe.GAME.Enums.GameStatus;
import com.example.TicTacToe.GAME.Enums.GameModes;
import com.example.TicTacToe.GAME.Enums.MoveType;
import com.example.TicTacToe.GAME.Enums.PatternType;
import com.example.TicTacToe.DTOs.GameDTOs.DTOGameMove;
import com.example.TicTacToe.GlobalException.GameExceptions.InvalidTurnException;
import com.example.TicTacToe.GlobalException.GameExceptions.MoveValidationFailException;
import com.example.TicTacToe.GlobalException.PlayerNotFoundException;
import com.example.TicTacToe.Models.Enums.MatchStatus;
import com.example.TicTacToe.Models.Enums.PlayerStatus;
import com.example.TicTacToe.Models.Match;
import com.example.TicTacToe.Models.MatchData;
import com.example.TicTacToe.Models.Player;
import com.example.TicTacToe.Repositories.MatchDataRepository;
import com.example.TicTacToe.Repositories.MatchRepository;
import com.example.TicTacToe.Repositories.PlayerRepositories;
import com.example.TicTacToe.Utils.JsonToString;
import com.example.TicTacToe.bot.BotEngine;
import com.example.TicTacToe.bot.BotMove;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.*;


public class GameSession {
    private static final Logger log = LoggerFactory.getLogger(GameSession.class);
    private final JsonToString jsonToString;
    private final MatchDataRepository matchDataRepository;
    private final PlayerRepositories playerRepositories;
    private final MatchRepository matchRepository;
    private final BotEngine botEngine;
    private final boolean botMatch;
    private final String botUsername;
    private Match match;
    private String sessionId;
    private String[][] board;
    private int boardSize;
    private String player1Username;
    private String player2Username;
    private String player1Symbol;
    private String player2Symbol;
    private String currentPlayer;
    private String winner;
    private int player1Points;
    private int player2Points;
    private GameStatus gameStatus;
    private GameEngine gameEngine; // Injected or static access
    private Set<String> pastPatterns;
    private int player1TwistTurnsLeft = 0;
    private int player2TwistTurnsLeft = 0;
    private boolean isTwistActive = false;
    private boolean isDoubleOverride = false;
    private int doubleOverrideTurns = 0;
    private List<String> newPattern = new ArrayList<>();
    private List<String> winPattern = new ArrayList<>();
    private DTOGameMove lastMove;
    private String lastMovePlayer;
    private String botError;

    public GameSession(int boardSize,
                       String player1Username, String player2Username, String player1Symbol,
                       String player2Symbol, JsonToString jsonToString, MatchDataRepository matchDataRepository, GameEngine gameEngine,
                       PlayerRepositories playerRepositories, MatchRepository matchRepository, String startingPlayer,
                       GameModes gameMode, BotEngine botEngine
    ) {
        this.sessionId = (UUID.randomUUID().toString());
        this.boardSize = boardSize;
        this.board = new String[boardSize][boardSize];
        this.player1Username = player1Username;
        this.player2Username = player2Username;
        this.player1Symbol = player1Symbol;
        this.player2Symbol = player2Symbol;
        this.currentPlayer = startingPlayer;
        this.player1Points = 0;
        this.player2Points = 0;
        this.gameStatus = GameStatus.ONGOING;
        this.pastPatterns = new HashSet<>();
        this.jsonToString = jsonToString;
        this.matchDataRepository = matchDataRepository;
        this.gameEngine = gameEngine;
        this.playerRepositories = playerRepositories;
        this.matchRepository = matchRepository;
        this.botMatch = gameMode == GameModes.BOTMATCH;
        this.botUsername = botMatch ? player2Username : null;
        this.botEngine = botEngine;
    }


    public synchronized boolean processMove(int i, int j, String symbol, MoveType moveType, PatternType patternType) {
        if (isBotTurn()) {
            throw new InvalidTurnException("Please wait for the bot's move");
        }
        return processSingleMove(i, j, symbol, moveType, patternType);
    }

    public synchronized boolean isBotTurn() {
        return botMatch && gameStatus == GameStatus.ONGOING && botUsername.equals(currentPlayer);
    }

    /**
     * Makes the bot's turn when this is a bot match and the bot is the current player.
     *
     * @return whether a bot move was made
     */
    public synchronized boolean playBotMoveIfNeeded() {
        if (!isBotTurn()) {
            return false;
        }

        String humanUsername = botUsername.equals(player1Username) ? player2Username : player1Username;
        String botSymbol = getSymbolForPlayer(botUsername);
        String humanSymbol = getSymbolForPlayer(humanUsername);
        BotMove move = botEngine.findBestMove(
                board,
                humanSymbol,
                botSymbol,
                getCurrentPlayerPoints(botUsername),
                getCurrentPlayerPoints(humanUsername),
                isDoubleOverride && botUsername.equals(lastMovePlayer) ? lastMove : null,
                isTwistActive
        );
        if (move == null) {
            throw new IllegalStateException("Bot could not select a move for a non-terminal game");
        }
        log.debug("Bot selected move for session {}: type={}, row={}, column={}, pattern={}",
                sessionId, move.getMoveType(), move.getI(), move.getJ(), move.getPatternType());

        boolean applied = processSingleMove(
                move.getI(),
                move.getJ(),
                botSymbol,
                move.getMoveType(),
                move.getPatternType()
        );
        if (!applied) {
            throw new IllegalStateException("Bot selected a move that GameEngine rejected");
        }
        return true;
    }

    private boolean processSingleMove(int i, int j, String symbol, MoveType moveType, PatternType patternType) {
        if (gameStatus == GameStatus.WON) return false;

        if (moveType == MoveType.TWIST && isTwistActive) {
            throw new InvalidTurnException("Twist is already active");
        }
        if (moveType == MoveType.TWIST && isDoubleOverride) {
            throw new InvalidTurnException("Complete the double override before using twist");
        }

        String currentPlayerSymbol = getSymbolForPlayer(currentPlayer);
        int currentPlayerPoints = getCurrentPlayerPoints(currentPlayer);
        if (!currentPlayerSymbol.equals(symbol) && !isTwistActive) {
            throw new InvalidTurnException("Invalid move, not your turn");
        }
        if (moveType == MoveType.TWIST) {
            currentPlayerSymbol = currentPlayer.equals(player1Username) ? player2Symbol : player1Symbol;
        }

        if (moveType == MoveType.DOUBLE_OVERRIDE && !isDoubleOverride) {
            isDoubleOverride = true;
            doubleOverrideTurns = 1;
        }

//         For Empty Pattern, remove past pattern of same pattern type row/col/diagonal
        if (moveType == MoveType.PATTERN_BOMB) {
            String matchStart = getString(i, j, patternType);
            pastPatterns.removeIf(pattern -> pattern.startsWith(matchStart));
        }

        //VALIDATE AND APPLY
        boolean success = gameEngine.applyMove(board, i, j, currentPlayerSymbol, moveType, patternType, currentPlayerPoints);
        if (!success) {
            log.debug("Move rejected for session {}: type={}, row={}, column={}", sessionId, moveType, i, j);
            return false;
        }

        if (moveType == MoveType.TWIST) {
            isTwistActive = true;
            player2TwistTurnsLeft = 2;
            player1TwistTurnsLeft = 2;
            log.info("Twist activated for session {}", sessionId);
        }
        log.debug("Move applied for session {}: type={}, row={}, column={}, pattern={}",
                sessionId, moveType, i, j, patternType);

        lastMove = new DTOGameMove(sessionId, i, j, currentPlayerSymbol, moveType, patternType);
        lastMovePlayer = currentPlayer;

        switch (moveType) {
            case NORMAL:
                break;
            case EMPTY_CELL, PATTERN_BOMB:
                deductPoints(currentPlayer, 2);
                break;
            case OVERRIDE:
                deductPoints(currentPlayer, 4);
                break;
            case DOUBLE_OVERRIDE: {
                if (doubleOverrideTurns == 0) {
                    deductPoints(currentPlayer, 5);
                    break;
                }
                break;
            }
            case TWIST:
                deductPoints(currentPlayer, 1);
                break;
        }

        //SAVE MATCHDATA
        MatchData matchData = new MatchData();
        matchData.setMatch(match);
        matchData.setX(i);
        matchData.setY(j);
        matchData.setSpecialMove(moveType != MoveType.NORMAL);
        matchData.setBoardSnapshot(jsonToString.convertJsonToString(board));
        matchData.setTimestamp(LocalDateTime.now());
        matchData.setPlayer(currentPlayer);
        matchDataRepository.save(matchData);

        //COUNT FOR PATTERNS
        List<String> currentPatterns = gameEngine.countThreePattern(board, currentPlayerSymbol);
        int newPoints = 0;
        newPattern.clear();
        for (String pattern : currentPatterns) {
            if (!pastPatterns.contains(pattern)) {
                pastPatterns.add(pattern);
                newPattern.add(pattern);
                newPoints++;
            }
        }

        //ADD POINTS TO PLAYER
        if (!isTwistActive && currentPlayerSymbol.equals(player1Symbol)) {
            player1Points = player1Points + newPoints;
        } else if (isTwistActive && currentPlayerSymbol.equals(player1Symbol)) {
            player2Points = player2Points + newPoints;
        } else if (!isTwistActive && currentPlayerSymbol.equals(player2Symbol)) {
            player2Points = player2Points + newPoints;
        } else if (isTwistActive && currentPlayerSymbol.equals(player2Symbol)) {
            player1Points = player1Points + newPoints;
        }

        //CHECK FOR WIN
        List<String> isWin = gameEngine.getWinningPatterns(board, i, j, currentPlayerSymbol);
        if (!isWin.isEmpty()) {
            gameStatus = GameStatus.WON;
            winner = currentPlayer;
            //END MATCH
            winPattern = isWin;
            match.setStatus(MatchStatus.FINISHED);
            match.setWinner(currentPlayer);
            match.setEndedAt(LocalDateTime.now());

            //PLAYER CHANGES
            Player winnerPlayer = playerRepositories.findPlayerByUsername(currentPlayer)
                    .orElseThrow(() -> new PlayerNotFoundException("Player not found"));
            String looserPlayerUsername = currentPlayer.equals(player1Username) ? player2Username : player1Username;
            Player looserPlayer = playerRepositories.findPlayerByUsername(looserPlayerUsername)
                    .orElseThrow(() -> new PlayerNotFoundException("Player not found"));

            //WINNER
            winnerPlayer.setPlayerStatus(PlayerStatus.ONLINE);
            winnerPlayer.setMMR(winnerPlayer.getMMR() + 50);
            winnerPlayer.setBehaviourScore(winnerPlayer.getBehaviourScore() + 10);
            winnerPlayer.setGamesPlayed(winnerPlayer.getGamesPlayed() + 1);
            winnerPlayer.setGamesWon(winnerPlayer.getGamesWon() + 1);

            //LOSER
            looserPlayer.setPlayerStatus(PlayerStatus.ONLINE);
            looserPlayer.setMMR(looserPlayer.getMMR() - 35);
            looserPlayer.setBehaviourScore(winnerPlayer.getBehaviourScore() + 10);
            looserPlayer.setGamesPlayed(looserPlayer.getGamesPlayed() + 1);
            looserPlayer.setGamesLost(looserPlayer.getGamesWon() + 1);

            playerRepositories.save(winnerPlayer);
            playerRepositories.save(looserPlayer);
            matchRepository.save(match);

            log.info("Game won for session {}", sessionId);
            return true;
        }

        //CHECK FOR DRAW
        boolean isDraw = gameEngine.isDraw(board);
        if (isDraw) {
            gameStatus = GameStatus.DRAW;
            log.info("Game drawn for session {}; expanding board from {} to {}", sessionId, boardSize, boardSize + 1);
            resetForNextBoard(boardSize + 1);
            return true;
        }

        //SWITCH PLAYER
        if (moveType == MoveType.DOUBLE_OVERRIDE && doubleOverrideTurns > 0 && isDoubleOverride) {
            doubleOverrideTurns--;
        } else {
            if (currentPlayer.equals(player1Username) && player1TwistTurnsLeft > 0) {
                player1TwistTurnsLeft--;
            } else if (currentPlayer.equals(player2Username) && player2TwistTurnsLeft > 0) {
                player2TwistTurnsLeft--;
            }
            if (isTwistActive && player1TwistTurnsLeft == 0 && player2TwistTurnsLeft == 0) {
                isTwistActive = false;
                log.info("Twist ended for session {}", sessionId);
            }
            switchTurn();
            isDoubleOverride = false;
            doubleOverrideTurns = 0;
        }


        return true;
    }

    private String getString(int i, int j, PatternType patternType) {
        String prefix = getPatternPrefix(patternType);
        if (prefix == null) {
            throw new MoveValidationFailException("Pattern type is missing");
        }

        int patternIndex = switch (patternType) {
            case NONE, MAIN_DIAGONAL -> 0;
            case ROW -> i;

            case COLUMN -> j;

            case LEFT_DIAGONAL -> {
                int row = i;
                int col = j;
                while (row > 0 && col > 0) {
                    row--;
                    col--;
                }
                yield row;
            }

            case RIGHT_DIAGONAL -> {
                int row = i;
                int col = j;
                while (row > 0 && col < board.length - 1) {
                    row--;
                    col++;
                }
                yield row;
            }
        };

        return prefix + "-" + patternIndex + "-";
    }

    private void switchTurn() {
        currentPlayer = currentPlayer.equals(player1Username) ? player2Username : player1Username;
    }

    public String getSymbolForPlayer(String currentPlayer) {
        if (currentPlayer.equals(player1Username)) {
            return isTwistActive ? player2Symbol : player1Symbol;
        } else {
            return isTwistActive ? player1Symbol : player2Symbol;
        }
    }

    public int getCurrentPlayerPoints(String currentPlayer) {
        if (currentPlayer.equals(player1Username)) {
            return player1Points;
        } else {
            return player2Points;
        }
    }

    private void deductPoints(String currentPlayer, int penalty) {
        if (currentPlayer.equals(player1Username)) {
            player1Points = Math.max(0, player1Points - penalty);
        } else {
            player2Points = Math.max(0, player2Points - penalty);
        }
    }

    public int getPoints(String symbol) {
        if (symbol.equals(player1Symbol)) return player1Points;
        if (symbol.equals(player2Symbol)) return player2Points;
        return 0;
    }

    private String getPatternPrefix(PatternType patternType) {
        return switch (patternType) {
            case ROW -> "H";
            case COLUMN -> "V";
            case LEFT_DIAGONAL -> "D";
            case RIGHT_DIAGONAL -> "A";
            case MAIN_DIAGONAL -> "MD";
            default -> null;
        };
    }

    public void resetForNextBoard(int newSize) {
        this.boardSize = newSize;
        this.board = new String[newSize][newSize];
        this.pastPatterns.clear();
        this.gameStatus = GameStatus.ONGOING;
        this.player1Points = 0;
        this.player2Points = 0;
        this.isTwistActive = false;
        this.isDoubleOverride = false;
        this.player1TwistTurnsLeft = 0;
        this.player2TwistTurnsLeft = 0;
        this.doubleOverrideTurns = 0;
        switchTurn();
    }

    public String getSessionId() {
        return sessionId;
    }

    public boolean isTwistActive() {
        return isTwistActive;
    }

    public void setTwistActive(boolean twistActive) {
        isTwistActive = twistActive;
    }

    public int getPlayer2TwistTurnsLeft() {
        return player2TwistTurnsLeft;
    }

    public String getWinner() {
        return winner;
    }

    public void setWinner(String winner) {
        this.winner = winner;
    }

    public boolean isDoubleOverride() {
        return isDoubleOverride;
    }

    public void setDoubleOverride(boolean doubleOverride) {
        isDoubleOverride = doubleOverride;
    }

    public int getDoubleOverrideTurns() {
        return doubleOverrideTurns;
    }

    public void setDoubleOverrideTurns(int doubleOverrideTurns) {
        this.doubleOverrideTurns = doubleOverrideTurns;
    }

    public void setPlayer2TwistTurnsLeft(int player2TwistTurnsLeft) {
        this.player2TwistTurnsLeft = player2TwistTurnsLeft;
    }

    public int getPlayer1TwistTurnsLeft() {
        return player1TwistTurnsLeft;
    }

    public void setPlayer1TwistTurnsLeft(int player1TwistTurnsLeft) {
        this.player1TwistTurnsLeft = player1TwistTurnsLeft;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String[][] getBoard() {
        return board;
    }

    public Match getMatch() {
        return match;
    }

    public void setMatch(Match match) {
        this.match = match;
    }

    public void setBoard(String[][] board) {
        this.board = board;
    }

    public int getBoardSize() {
        return boardSize;
    }

    public void setBoardSize(int boardSize) {
        this.boardSize = boardSize;
    }

    public String getPlayer1Username() {
        return player1Username;
    }

    public void setPlayer1Username(String player1Username) {
        this.player1Username = player1Username;
    }

    public String getPlayer2Username() {
        return player2Username;
    }

    public void setPlayer2Username(String player2Username) {
        this.player2Username = player2Username;
    }

    public String getPlayer1Symbol() {
        return player1Symbol;
    }

    public void setPlayer1Symbol(String player1Symbol) {
        this.player1Symbol = player1Symbol;
    }

    public String getPlayer2Symbol() {
        return player2Symbol;
    }

    public void setPlayer2Symbol(String player2Symbol) {
        this.player2Symbol = player2Symbol;
    }

    public String getCurrentPlayer() {
        return currentPlayer;
    }

    public void setCurrentPlayer(String currentPlayer) {
        this.currentPlayer = currentPlayer;
    }

    public int getPlayer1Points() {
        return player1Points;
    }

    public void setPlayer1Points(int player1Points) {
        this.player1Points = player1Points;
    }

    public int getPlayer2Points() {
        return player2Points;
    }

    public void setPlayer2Points(int player2Points) {
        this.player2Points = player2Points;
    }

    public GameStatus getGameStatus() {
        return gameStatus;
    }

    public void setGameStatus(GameStatus gameStatus) {
        this.gameStatus = gameStatus;
    }

    public GameEngine getGameEngine() {
        return gameEngine;
    }

    public void setGameEngine(GameEngine gameEngine) {
        this.gameEngine = gameEngine;
    }

    public Set<String> getPastPatterns() {
        return pastPatterns;
    }

    public void setPastPatterns(Set<String> pastPatterns) {
        this.pastPatterns = pastPatterns;
    }

    public List<String> getNewPattern() {
        return newPattern;
    }

    public void setNewPattern(List<String> newPattern) {
        this.newPattern = newPattern;
    }

    public List<String> getWinPattern() {
        return winPattern;
    }

    public DTOGameMove getLastMove() {
        return lastMove;
    }

    public String getLastMovePlayer() {
        return lastMovePlayer;
    }

    public String getBotError() {
        return botError;
    }

    public void setBotError(String botError) {
        this.botError = botError;
    }

    public void setWinPattern(List<String> winPattern) {
        this.winPattern = winPattern;
    }
}
