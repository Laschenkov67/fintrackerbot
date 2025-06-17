package com.example.fintrackerbot.handler;

import com.example.fintrackerbot.services.MetalService;
import com.example.fintrackerbot.services.TelegramMessageService;
import org.springframework.stereotype.Component;

@Component
public class MetalCommandHandler extends DataCommandHandler {

    private final MetalService metalService;

    public MetalCommandHandler(TelegramMessageService messageService, MetalService metalService) {
        super(messageService);
        this.metalService = metalService;
    }

    @Override
    protected String fetchData() throws Exception {
        return metalService.formatMetalPrices(metalService.getMetalPrices());
    }

    @Override
    protected String getErrorMessage() {
        return "Ошибка получения цен на металлы";
    }

    @Override
    public String getCommand() {
        return "Драгоценные металлы";
    }
}
