package com.example.fintrackerbot;

import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@Component
public class UpdateConsumer implements LongPollingSingleThreadUpdateConsumer {

    private final TelegramClient telegramClient;

    private final Map<String, Supplier<String>> commandHandlers = Map.of(
            "Курсы валют", BusinessDataGenerator::generateJuridicalInn,
            "Курсы криптовалют", BusinessDataGenerator::generateIndividualInn,
            "Драгоценные металлы", BusinessDataGenerator::generateOgrn,
            "Нефть", BusinessDataGenerator::generateOgrnIp
    );

    public UpdateConsumer() {
        this.telegramClient = new OkHttpTelegramClient(""); // добавь токен
    }

    @SneakyThrows
    @Override
    public void consume(Update update) {
        if (!update.hasMessage()) return;

        var message = update.getMessage();
        var chatId = message.getChatId();
        var text = message.getText();

        if ("/start".equals(text)) {
            sendReplyKeyboard(chatId);
        } else {
            Supplier<String> handler = commandHandlers.get(text);
            if (handler != null) {
                sendMessage(chatId, handler.get());
            } else {
                sendMessage(chatId, "Я вас не понимаю");
            }
        }
    }

    @SneakyThrows
    private void sendReplyKeyboard(Long chatId) {
        List<String> buttons = List.of(
                "Курсы валют", "Курсы криптовалют", "Золото",
                "Нефть"
        );

        List<KeyboardRow> keyboard = new ArrayList<>();
        for (int i = 0; i < buttons.size(); i += 3) {
            KeyboardRow row = new KeyboardRow();
            row.addAll(buttons.subList(i, Math.min(i + 3, buttons.size())));
            keyboard.add(row);
        }

        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup(keyboard);
        markup.setResizeKeyboard(true);
        markup.setOneTimeKeyboard(false);

        SendMessage message = SendMessage.builder()
                .chatId(chatId.toString())
                .text("Выберите значение из списка:")
                .replyMarkup(markup)
                .build();

        telegramClient.execute(message);
    }

    @SneakyThrows
    private void sendMessage(Long chatId, String text) {
        SendMessage message = SendMessage.builder()
                .chatId(chatId.toString())
                .text(text)
                .build();

        telegramClient.execute(message);
    }
}