package com.example.fintrackerbot.services;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@Service
public class BotCommandHandlerService {

    private static final List<String> BUTTONS = List.of(
            "Курсы валют", "Курсы криптовалют", "Драгоценные металлы", "Нефть");
    private static final int BUTTONS_PER_ROW = 2;

    private final CurrencyService currencyService;
    private final CryptoService cryptoService;
    private final MetalService metalService;
    private final OilService oilService;

    private final Map<String, Supplier<String>> commandHandlers;

    public BotCommandHandlerService(
            CurrencyService currencyService,
            CryptoService cryptoService,
            MetalService metalService,
            OilService oilService
    ) {
        this.currencyService = currencyService;
        this.cryptoService = cryptoService;
        this.metalService = metalService;
        this.oilService = oilService;
        this.commandHandlers = initHandlers();
    }

    public String handleCommand(String command) {
        Supplier<String> handler = commandHandlers.get(command);
        return handler != null ? handler.get() : null;
    }

    private Map<String, Supplier<String>> initHandlers() {
        Map<String, Supplier<String>> map = new HashMap<>();

        map.put("/start", this::buildReplyKeyboardMessage);
        map.put("Курсы валют", () -> safeExecute(
                () -> {
                    try {
                        return currencyService.formatCurrencyRates(currencyService.getCurrencyRates());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                },
                "Ошибка получения курсов валют"
        ));
        map.put("Курсы криптовалют", () -> safeExecute(
                () -> {
                    try {
                        return cryptoService.formatCryptoRates(cryptoService.getCryptoRates());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                },
                "Ошибка получения курсов криптовалют"
        ));
        map.put("Драгоценные металлы", () -> safeExecute(
                () -> {
                    try {
                        return metalService.formatMetalPrices(metalService.getMetalPrices());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                },
                "Ошибка получения цен на металлы"
        ));
        map.put("Нефть", () -> safeExecute(
                () -> oilService.formatOilPrices(oilService.getOilPrices()),
                "Ошибка получения цен на нефть"
        ));

        return map;
    }

    private String buildReplyKeyboardMessage() {
        StringBuilder sb = new StringBuilder("Выберите значение из списка:\n");
        for (int i = 0; i < BUTTONS.size(); i++) {
            sb.append(BUTTONS.get(i)).append((i % BUTTONS_PER_ROW == 1 || i == BUTTONS.size() - 1) ? "\n" : " | ");
        }
        return sb.toString();
    }

    private String safeExecute(Supplier<String> supplier, String fallbackMessage) {
        try {
            return supplier.get();
        } catch (Exception e) {
            return fallbackMessage;
        }
    }
}
