package com.example.fintrackerbot;

import com.example.fintrackerbot.config.TelegramClientConfig;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
@Component
public class FinTrackerBot implements SpringLongPollingBot {
    private final UpdateConsumer updateConsumer;
    private final TelegramClientConfig telegramClientConfig;

    public FinTrackerBot(UpdateConsumer updateConsumer, TelegramClientConfig telegramClientConfig) {
        this.updateConsumer = updateConsumer;
        this.telegramClientConfig = telegramClientConfig;
    }

    @Override
    public String getBotToken() {
        return telegramClientConfig.botToken;
    }

    @Override
    public LongPollingUpdateConsumer getUpdatesConsumer() {
        return updateConsumer;
    }
}
