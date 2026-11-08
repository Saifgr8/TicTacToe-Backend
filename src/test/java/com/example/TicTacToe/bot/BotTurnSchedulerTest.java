package com.example.TicTacToe.bot;

import com.example.TicTacToe.DTOs.GameDTOs.DTOGameSessionResponse;
import com.example.TicTacToe.GAME.Enums.GameModes;
import com.example.TicTacToe.GAME.Enums.MoveType;
import com.example.TicTacToe.GAME.Enums.PatternType;
import com.example.TicTacToe.GAME.GameEngine;
import com.example.TicTacToe.GAME.GameSession;
import com.example.TicTacToe.GAME.GameSessionSnapshot;
import com.example.TicTacToe.GAME.UTILS.GetAllAntiDiagonalPairs;
import com.example.TicTacToe.GAME.UTILS.GetAllDiagonalMain;
import com.example.TicTacToe.GAME.UTILS.GetAllDiagonalPairsWithoutMain;
import com.example.TicTacToe.Models.Match;
import com.example.TicTacToe.Models.Player;
import com.example.TicTacToe.Repositories.MatchDataRepository;
import com.example.TicTacToe.Repositories.MatchRepository;
import com.example.TicTacToe.Repositories.PlayerRepositories;
import com.example.TicTacToe.Utils.JsonToString;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Instant;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class BotTurnSchedulerTest {
    @Test
    void springRegistersTheBotScheduler() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.registerBean(SimpMessagingTemplate.class, () -> mock(SimpMessagingTemplate.class));
            context.register(BotTurnScheduler.class);
            context.refresh();
            assertNotNull(context.getBean(BotTurnScheduler.class));
            assertNotNull(context.getBean("botTaskScheduler", ThreadPoolTaskScheduler.class));
        }
    }

    @Test
    void waitsForTransactionCommitBeforeScheduling() {
        ThreadPoolTaskScheduler executor = mock(ThreadPoolTaskScheduler.class);
        BotTurnScheduler scheduler = new BotTurnScheduler(executor, mock(SimpMessagingTemplate.class), 1500);
        GameSession session = new GameSession(3, "human", "bot", "X", "O", mock(JsonToString.class),
                mock(MatchDataRepository.class), mock(GameEngine.class), mock(PlayerRepositories.class),
                mock(MatchRepository.class), "bot", GameModes.BOTMATCH, mock(BotEngine.class));
        TransactionSynchronizationManager.initSynchronization();
        TransactionSynchronizationManager.setActualTransactionActive(true);
        try {
            scheduler.scheduleAfterCommit(session);
            verifyNoInteractions(executor);
            for (TransactionSynchronization synchronization : TransactionSynchronizationManager.getSynchronizations()) {
                synchronization.afterCommit();
            }
            verify(executor).schedule(any(Runnable.class), any(Instant.class));
        } finally {
            TransactionSynchronizationManager.clear();
        }
    }

    @Test
    void schedulesSecondDoubleOverrideTurnSeparately() {
        ThreadPoolTaskScheduler executor = mock(ThreadPoolTaskScheduler.class);
        SimpMessagingTemplate messaging = mock(SimpMessagingTemplate.class);
        PlayerRepositories players = mock(PlayerRepositories.class);
        when(players.findPlayerByUsername("human"))
                .thenReturn(Optional.of(new Player("human@example.com", "human", "password")));
        when(players.findPlayerByUsername("bot"))
                .thenReturn(Optional.of(new Player("bot@example.com", "bot", "password")));
        GameSession session = new GameSession(3, "human", "bot", "X", "O", mock(JsonToString.class),
                mock(MatchDataRepository.class),
                new GameEngine(new GetAllDiagonalPairsWithoutMain(), new GetAllAntiDiagonalPairs(),
                        new GetAllDiagonalMain()), players, mock(MatchRepository.class),
                "bot", GameModes.BOTMATCH, new BotEngine());
        session.setMatch(new Match());
        session.setPlayer2Points(5);
        session.setTwistActive(true);
        session.setPlayer1TwistTurnsLeft(2);
        session.setPlayer2TwistTurnsLeft(2);
        session.setBoard(new String[][] {{"X", "O", "O"}, {null, null, null}, {null, null, null}});
        BotTurnScheduler scheduler = new BotTurnScheduler(executor, messaging, 1500);

        scheduler.scheduleAfterCommit(session);
        ArgumentCaptor<Runnable> tasks = ArgumentCaptor.forClass(Runnable.class);
        verify(executor).schedule(tasks.capture(), any(Instant.class));
        tasks.getValue().run();
        assertTrue(session.isBotTurn());
        verify(executor, times(2)).schedule(tasks.capture(), any(Instant.class));
        tasks.getAllValues().get(tasks.getAllValues().size() - 1).run();

        assertEquals("bot", session.getWinner());
        verify(messaging, times(2)).convertAndSend(anyString(), any(DTOGameSessionResponse.class));
    }

    @Test
    void actualSchedulerWaitsBeforeExecutingBotMove() throws InterruptedException {
        ThreadPoolTaskScheduler executor = new ThreadPoolTaskScheduler();
        executor.setPoolSize(1);
        executor.initialize();
        try {
            SimpMessagingTemplate messaging = mock(SimpMessagingTemplate.class);
            CountDownLatch delivered = new CountDownLatch(1);
            doAnswer(invocation -> {
                delivered.countDown();
                return null;
            }).when(messaging).convertAndSend(anyString(), any(DTOGameSessionResponse.class));
            JsonToString json = mock(JsonToString.class);
            GameSession session = new GameSession(3, "human", "bot", "X", "O", json,
                    mock(MatchDataRepository.class),
                    new GameEngine(new GetAllDiagonalPairsWithoutMain(), new GetAllAntiDiagonalPairs(),
                            new GetAllDiagonalMain()), mock(PlayerRepositories.class), mock(MatchRepository.class),
                    "bot", GameModes.BOTMATCH, new BotEngine());
            BotTurnScheduler scheduler = new BotTurnScheduler(executor, messaging, 1500);
            long start = System.nanoTime();
            scheduler.scheduleAfterCommit(session);

            assertTrue(delivered.await(5, TimeUnit.SECONDS));
            long elapsedMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
            assertTrue(elapsedMillis >= 1500, "Bot moved after only " + elapsedMillis + " ms");
            assertEquals("human", GameSessionSnapshot.from(session).getCurrentPlayer());
        } finally {
            executor.shutdown();
        }
    }

    @Test
    void schedulesOnlyOneDelayedMoveAndPublishesItsDetails() {
        ThreadPoolTaskScheduler executor = mock(ThreadPoolTaskScheduler.class);
        SimpMessagingTemplate messaging = mock(SimpMessagingTemplate.class);
        BotTurnScheduler scheduler = new BotTurnScheduler(executor, messaging, 1500);
        JsonToString json = mock(JsonToString.class);
        when(json.convertJsonToString(any())).thenReturn("{}");
        GameSession session = new GameSession(3, "human", "bot", "X", "O", json,
                mock(MatchDataRepository.class),
                new GameEngine(new GetAllDiagonalPairsWithoutMain(), new GetAllAntiDiagonalPairs(),
                        new GetAllDiagonalMain()), mock(PlayerRepositories.class), mock(MatchRepository.class),
                "human", GameModes.BOTMATCH, new BotEngine());
        session.processMove(0, 0, "X", MoveType.NORMAL, PatternType.NONE);
        Instant before = Instant.now();

        scheduler.scheduleAfterCommit(session);
        scheduler.scheduleAfterCommit(session);

        ArgumentCaptor<Runnable> task = ArgumentCaptor.forClass(Runnable.class);
        ArgumentCaptor<Instant> scheduledTime = ArgumentCaptor.forClass(Instant.class);
        verify(executor, times(1)).schedule(task.capture(), scheduledTime.capture());
        assertFalse(scheduledTime.getValue().isBefore(before.plusMillis(1500)));
        assertTrue(GameSessionSnapshot.from(session).isBotThinking());
        task.getValue().run();

        ArgumentCaptor<DTOGameSessionResponse> response = ArgumentCaptor.forClass(DTOGameSessionResponse.class);
        verify(messaging).convertAndSend(eq("/topic/game/" + session.getSessionId()), response.capture());
        assertEquals("bot", response.getValue().getLastMovePlayer());
        assertEquals("O", response.getValue().getLastMove().getSymbol());
        assertEquals("human", response.getValue().getCurrentPlayer());
        assertFalse(response.getValue().isBotThinking());
    }

    @Test
    void reportsBotErrorsToPollingAndWebSocketClients() {
        ThreadPoolTaskScheduler executor = mock(ThreadPoolTaskScheduler.class);
        SimpMessagingTemplate messaging = mock(SimpMessagingTemplate.class);
        BotEngine bot = mock(BotEngine.class);
        JsonToString json = mock(JsonToString.class);
        GameSession session = new GameSession(3, "human", "bot", "X", "O", json,
                mock(MatchDataRepository.class), mock(GameEngine.class), mock(PlayerRepositories.class),
                mock(MatchRepository.class), "bot", GameModes.BOTMATCH, bot);
        BotTurnScheduler scheduler = new BotTurnScheduler(executor, messaging, 1500);
        scheduler.scheduleAfterCommit(session);
        ArgumentCaptor<Runnable> task = ArgumentCaptor.forClass(Runnable.class);
        verify(executor).schedule(task.capture(), any(Instant.class));

        task.getValue().run();

        DTOGameSessionResponse snapshot = GameSessionSnapshot.from(session);
        assertNotNull(snapshot.getBotError());
        assertFalse(snapshot.isBotThinking());
        verify(messaging).convertAndSend(eq("/topic/game/" + session.getSessionId()),
                any(DTOGameSessionResponse.class));
        verify(executor, times(1)).schedule(any(Runnable.class), any(Instant.class));
    }
}
