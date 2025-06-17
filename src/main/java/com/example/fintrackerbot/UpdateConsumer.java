package com.example.fintrackerbot;

import com.example.fintrackerbot.services.CommandService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class UpdateConsumer implements LongPollingSingleThreadUpdateConsumer {

    private final CommandService commandService;

    public UpdateConsumer(CommandService commandService) {
        this.commandService = commandService;
    }

    @Override
    public void consume(Update update) {
        if (!update.hasMessage() || update.getMessage().getText() == null) {
            return;
        }

        Long chatId = update.getMessage().getChatId();
        String text = update.getMessage().getText();

        commandService.processCommand(chatId, text);
    }
}