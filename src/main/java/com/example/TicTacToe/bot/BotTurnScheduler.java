package com.example.TicTacToe.bot;

import com.example.TicTacToe.GAME.GameSession;
import com.example.TicTacToe.GAME.GameSessionSnapshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Instant;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class BotTurnScheduler {
    private static final Logger log = LoggerFactory.getLogger(BotTurnScheduler.class);

    private final ThreadPoolTaskScheduler botTaskScheduler;
    private final SimpMessagingTemplate messagingTemplate;
    private final long delayMillis;
    private final Set<GameSession> pending = ConcurrentHashMap.newKeySet();

    public BotTurnScheduler(ThreadPoolTaskScheduler botTaskScheduler, SimpMessagingTemplate messagingTemplate,
                            @Value("${game.bot.delay-ms:1500}") long delayMillis) {
        if (delayMillis < 1) {
            throw new IllegalArgumentException("game.bot.delay-ms must be positive");
        }
        this.botTaskScheduler = botTaskScheduler;
        this.messagingTemplate = messagingTemplate;
        this.delayMillis = delayMillis;
    }

    public void scheduleAfterCommit(GameSession session) {
        if (TransactionSynchronizationManager.isActualTransactionActive()
                && TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    schedule(session);
                }
            });
        } else {
            schedule(session);
        }
    }

    private void schedule(GameSession session) {
        synchronized (session) {
            if (!session.isBotTurn() || session.getBotError() != null || !pending.add(session)) {
                return;
            }
            try {
                botTaskScheduler.schedule(() -> playTurn(session), Instant.now().plusMillis(delayMillis));
                log.debug("Scheduled bot turn for session {} with delay {} ms", session.getSessionId(), delayMillis);
            } catch (RuntimeException exception) {
                pending.remove(session);
                reportFailure(session, exception);
            }
        }
    }

    private void playTurn(GameSession session) {
        synchronized (session) {
            try {
                if (session.playBotMoveIfNeeded()) {
                    log.debug("Bot turn completed for session {}", session.getSessionId());
                    publish(session);
                }
            } catch (RuntimeException exception) {
                reportFailure(session, exception);
            } finally {
                pending.remove(session);
            }
            schedule(session);
        }
    }

    private void reportFailure(GameSession session, RuntimeException exception) {
        log.error("Bot turn failed for session {}", session.getSessionId(), exception);
        session.setBotError("The bot could not complete its move.");
        publish(session);
    }

    private void publish(GameSession session) {
        try {
            messagingTemplate.convertAndSend(
                    "/topic/game/" + session.getSessionId(), GameSessionSnapshot.from(session));
        } catch (RuntimeException exception) {
            log.error("Could not publish game update for session {}; state remains available by polling",
                    session.getSessionId(), exception);
        }
    }

    @Configuration
    static class SchedulerConfiguration {
        @Bean
        ThreadPoolTaskScheduler botTaskScheduler() {
            ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
            scheduler.setPoolSize(2);
            scheduler.setThreadNamePrefix("bot-turn-");
            return scheduler;
        }
    }
}
