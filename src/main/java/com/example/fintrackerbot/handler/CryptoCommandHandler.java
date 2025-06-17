package com.example.fintrackerbot.handler;

import com.example.fintrackerbot.services.CryptoService;
import com.example.fintrackerbot.services.TelegramMessageService;
import org.springframework.stereotype.Component;

@Component
public class CryptoCommandHandler extends DataCommandHandler {

    private final CryptoService cryptoService;

    public CryptoCommandHandler(TelegramMessageService messageService, CryptoService cryptoService) {
        super(messageService);
        this.cryptoService = cryptoService;
    }

    @Override
    protected String fetchData() throws Exception {
        return cryptoService.formatCryptoRates(cryptoService.getCryptoRates());
    }

    @Override
    protected String getErrorMessage() {
        return "Ошибка получения курсов криптовалют";
    }

    @Override
    public String getCommand() {
        return "Курсы криптовалют";
    }
}
