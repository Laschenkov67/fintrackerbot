package com.example.fintrackerbot;
import com.example.fintrackerbot.services.LoggingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.mockito.Mockito.*;

class LoggingServiceTest {

    private Logger mockLogger;
    private LoggingService loggingService;

    @BeforeEach
    void setUp() {
        mockLogger = mock(Logger.class);

        // Используем mockStatic для перехвата LoggerFactory.getLogger(...)
        try (MockedStatic<LoggerFactory> mockedStatic = mockStatic(LoggerFactory.class)) {
            mockedStatic.when(() -> LoggerFactory.getLogger(LoggingService.class))
                    .thenReturn(mockLogger);
            loggingService = new LoggingService(); // теперь в нем будет mockLogger
        }
    }

    @Test
    void testLogInfo() {
        loggingService.logInfo("Test info");
        verify(mockLogger).info("Test info");
    }

    @Test
    void testLogError() {
        Throwable throwable = new RuntimeException("Test exception");
        loggingService.logError("Test error", throwable);
        verify(mockLogger).error("Test error", throwable);
    }

    @Test
    void testLogWarn() {
        loggingService.logWarn("Test warn");
        verify(mockLogger).warn("Test warn");
    }

    @Test
    void testLogDebug() {
        loggingService.logDebug("Test debug");
        verify(mockLogger).debug("Test debug");
    }

    @Test
    void testLogTrace() {
        loggingService.logTrace("Test trace");
        verify(mockLogger).trace("Test trace");
    }
}
