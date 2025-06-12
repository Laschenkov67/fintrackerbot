package com.example.fintrackerbot.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class LoggingService {
    private static final Logger logger = LoggerFactory.getLogger(LoggingService.class);

    public void logInfo(String message) {
        logger.info(message);
    }

    public void logError(String message, Throwable throwable) {
        logger.error(message, throwable);
    }

    public void logWarn(String message) {
        logger.warn(message);
    }

    public void logDebug(String message) {
        logger.debug(message);
    }

    public void logTrace(String message) {
        logger.trace(message);
    }
}
