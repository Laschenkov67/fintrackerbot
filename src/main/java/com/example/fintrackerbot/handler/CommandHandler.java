package com.example.fintrackerbot.handler;

public interface CommandHandler {
    void handle(Long chatId);
    String getCommand();
}