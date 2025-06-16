package com.example.fintrackerbot.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Configuration
public class TelegramClientConfig {

    @Value("${telegram.bot.token}")
    public String botToken;

    @Bean
    public TelegramClient telegramClient() {
        return new OkHttpTelegramClient(botToken);
    }
}