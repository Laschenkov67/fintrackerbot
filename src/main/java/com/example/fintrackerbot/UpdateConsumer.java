package com.example.fintrackerbot;

import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Component
public class UpdateConsumer implements LongPollingSingleThreadUpdateConsumer {

    private static final List<String> BUTTONS = List.of(
            "Курсы валют", "Курсы криптовалют", "Драгоценные металлы", "Нефть");
    private static final int BUTTONS_PER_ROW = 2;

    private final TelegramClient telegramClient;
    private final Map<String, Consumer<Long>> commandHandlers;

    private Map<String, Consumer<Long>> initializeCommandHandlers() {
        Map<String, Consumer<Long>> handlers = new HashMap<>();

        handlers.put("/start", this::sendReplyKeyboard);
        handlers.put("Курсы валют", this::getCurrencyRates);
        handlers.put("Курсы криптовалют", this::getCryptoRates);
        handlers.put("Драгоценные металлы", this::getMetalRates);
        handlers.put("Нефть", this::getOilPrice);

        return handlers;
    }

    public UpdateConsumer() {
        this.telegramClient = new OkHttpTelegramClient("");
        this.commandHandlers = initializeCommandHandlers();
    }

    @SneakyThrows
    @Override
    public void consume(Update update) {
        if (!update.hasMessage()) {
            return;
        }

        var message = update.getMessage();
        var chatId = message.getChatId();
        var text = message.getText();

        if (text == null) {
            return;
        }

        Consumer<Long> handler = commandHandlers.getOrDefault(text,
                id -> sendMessage(id, "Я вас не понимаю"));
        handler.accept(chatId);
    }

    @SneakyThrows
    private void sendReplyKeyboard(Long chatId) {
        var keyboardRows = new ArrayList<KeyboardRow>();

        for (int i = 0; i < BUTTONS.size(); i += BUTTONS_PER_ROW) {
            var row = new KeyboardRow();
            BUTTONS.stream()
                    .skip(i)
                    .limit(BUTTONS_PER_ROW)
                    .forEach(row::add);
            keyboardRows.add(row);
        }

        var markup = ReplyKeyboardMarkup.builder()
                .keyboard(keyboardRows)
                .resizeKeyboard(true)
                .oneTimeKeyboard(false)
                .build();

        var message = SendMessage.builder()
                .chatId(chatId.toString())
                .text("Выберите значение из списка:")
                .replyMarkup(markup)
                .build();

        try {
            telegramClient.execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @SneakyThrows
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

    private void sendGeneratedValue(Long chatId, Supplier<String> generator) {
        String value = generator.get();
        sendMessage(chatId, value);
    }

    private void getCurrencyRates (Long chatId) {
        //sendGeneratedValue(chatId, BusinessDataGenerator::generateJuridicalInn);
    }

    private void getCryptoRates(Long chatId) {
        //sendGeneratedValue(chatId, BusinessDataGenerator::generateIndividualInn);
    }

    private void getMetalRates(Long chatId) {
        //sendGeneratedValue(chatId, BusinessDataGenerator::generateOgrn);
    }

    private void getOilPrice(Long chatId) {
        //sendGeneratedValue(chatId, BusinessDataGenerator::generateOgrnIp);
    }
}