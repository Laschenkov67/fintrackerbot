package com.example.fintrackerbot.services;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.text.DecimalFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class CurrencyService extends MarketQuoteService {

    @Value("${currency.api.url}")
    private String apiUrl;

    private final DecimalFormat df = new DecimalFormat("#.####");

    public CurrencyService(RestTemplate restTemplate) {
        super(restTemplate);
    }

    public Map<String, Double> getCurrencyRates() throws Exception {
        JsonNode ratesNode = getJsonFromUrl(apiUrl).path("rates");

        Map<String, Double> rates = new LinkedHashMap<>();
        for (String currency : List.of("USD", "EUR", "JPY", "CNY", "GBP")) {
            rates.put(currency, 1 / ratesNode.path(currency).asDouble());
        }
        return rates;
    }

    public String formatCurrencyRates(Map<String, Double> rates) {
        return formatTable(rates, "Курсы валют к RUB:", "Валюта", "Курс", df);
    }
}
