package com.example.TicTacToe.GlobalException;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerLoggingTest {
    @Test
    void logsUnhandledExceptionsWithTheirStackTrace() {
        Logger logger = (Logger) LoggerFactory.getLogger(GlobalExceptionHandler.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        try {
            RuntimeException exception = new IllegalStateException("test failure");
            var response = new GlobalExceptionHandler().runtimeException(exception);

            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
            assertEquals(1, appender.list.size());
            ILoggingEvent event = appender.list.get(0);
            assertEquals(Level.ERROR, event.getLevel());
            assertNotNull(event.getThrowableProxy());
            assertEquals(IllegalStateException.class.getName(), event.getThrowableProxy().getClassName());
        } finally {
            logger.detachAppender(appender);
            appender.stop();
        }
    }
}
