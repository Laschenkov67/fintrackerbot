package com.example.fintrackerbot.handler;

import com.example.fintrackerbot.services.TelegramMessageService;

public abstract class DataCommandHandler implements CommandHandler {

    protected final TelegramMessageService messageService;

    protected DataCommandHandler(TelegramMessageService messageService) {
        this.messageService = messageService;
    }

    @Override
    public void handle(Long chatId) {
        try {
            String data = fetchData();
            messageService.sendMessage(chatId, data);
        } catch (Exception e) {
            messageService.sendMessage(chatId, getErrorMessage());
        }
    }

    protected abstract String fetchData() throws Exception;
    protected abstract String getErrorMessage();
}
