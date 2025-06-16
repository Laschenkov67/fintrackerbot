package com.example.fintrackerbot;

import com.example.fintrackerbot.services.BotCommandHandlerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Component
public class UpdateConsumer implements LongPollingSingleThreadUpdateConsumer {

    private final TelegramClient telegramClient;
    private final BotCommandHandlerService commandHandlerService;

    @Autowired
    public UpdateConsumer(TelegramClient telegramClient, BotCommandHandlerService commandHandlerService) {
        this.telegramClient = telegramClient;
        this.commandHandlerService = commandHandlerService;
    }

    @Override
    public void consume(Update update) {
        if (!update.hasMessage() || update.getMessage().getText() == null) return;

        Long chatId = update.getMessage().getChatId();
        String text = update.getMessage().getText();

        String response = commandHandlerService.handleCommand(text);
        if (response == null) {
            response = "Я вас не понимаю";
        }

        sendMessage(chatId, response);
    }

    private void sendMessage(Long chatId, String messageText) {
        var message = SendMessage.builder()
                .text(messageText)
                .chatId(chatId.toString())
                .build();
        try {
            telegramClient.execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}