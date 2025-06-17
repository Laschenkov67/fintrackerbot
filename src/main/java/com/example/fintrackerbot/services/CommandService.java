package com.example.fintrackerbot.services;

import com.example.fintrackerbot.handler.CommandHandler;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class CommandService {

    private final Map<String, CommandHandler> commandHandlers;
    private final TelegramMessageService messageService;

    public CommandService(List<CommandHandler> handlers, TelegramMessageService messageService) {
        this.messageService = messageService;
        this.commandHandlers = handlers.stream()
                .collect(Collectors.toMap(CommandHandler::getCommand, Function.identity()));
    }

    public void processCommand(Long chatId, String command) {
        CommandHandler handler = commandHandlers.get(command);

        if (handler != null) {
            handler.handle(chatId);
        } else {
            messageService.sendMessage(chatId, "Я вас не понимаю");
        }
    }
}