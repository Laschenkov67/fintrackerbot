package com.example.fintrackerbot.handler;

import com.example.fintrackerbot.services.CurrencyService;
import com.example.fintrackerbot.services.TelegramMessageService;
import org.springframework.stereotype.Component;

@Component
public class CurrencyCommandHandler extends DataCommandHandler {

    private final CurrencyService currencyService;

    public CurrencyCommandHandler(TelegramMessageService messageService, CurrencyService currencyService) {
        super(messageService);
        this.currencyService = currencyService;
    }

    @Override
    protected String fetchData() throws Exception {
        return currencyService.formatCurrencyRates(currencyService.getCurrencyRates());
    }

    @Override
    protected String getErrorMessage() {
        return "Ошибка получения курсов валют";
    }

    @Override
    public String getCommand() {
        return "Курсы валют";
    }
}
