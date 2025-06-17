package com.example.fintrackerbot.handler;

import com.example.fintrackerbot.services.OilService;
import com.example.fintrackerbot.services.TelegramMessageService;
import org.springframework.stereotype.Component;

@Component
public class OilCommandHandler extends DataCommandHandler {

    private final OilService oilService;

    public OilCommandHandler(TelegramMessageService messageService, OilService oilService) {
        super(messageService);
        this.oilService = oilService;
    }

    @Override
    protected String fetchData() {
        return oilService.formatOilPrices(oilService.getOilPrices());
    }

    @Override
    protected String getErrorMessage() {
        return "Ошибка получения цен на нефть";
    }

    @Override
    public String getCommand() {
        return "Нефть";
    }
}
