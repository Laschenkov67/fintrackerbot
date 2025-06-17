package com.example.fintrackerbot.handler;

import com.example.fintrackerbot.services.TelegramMessageService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

@Component
public class StartCommandHandler implements CommandHandler {

    private static final List<String> BUTTONS = List.of(
            "Курсы валют", "Курсы криптовалют", "Драгоценные металлы", "Нефть");
    private static final int BUTTONS_PER_ROW = 2;

    private final TelegramMessageService messageService;

    public StartCommandHandler(TelegramMessageService messageService) {
        this.messageService = messageService;
    }

    @Override
    public void handle(Long chatId) {
        ReplyKeyboardMarkup keyboard = createKeyboard();
        messageService.sendMessageWithKeyboard(chatId, "Выберите значение из списка:", keyboard);
    }

    @Override
    public String getCommand() {
        return "/start";
    }

    private ReplyKeyboardMarkup createKeyboard() {
        var keyboardRows = new ArrayList<KeyboardRow>();

        for (int i = 0; i < BUTTONS.size(); i += BUTTONS_PER_ROW) {
            var row = new KeyboardRow();
            BUTTONS.stream()
                    .skip(i)
                    .limit(BUTTONS_PER_ROW)
                    .forEach(row::add);
            keyboardRows.add(row);
        }

        return ReplyKeyboardMarkup.builder()
                .keyboard(keyboardRows)
                .resizeKeyboard(true)
                .oneTimeKeyboard(false)
                .build();
    }
}
