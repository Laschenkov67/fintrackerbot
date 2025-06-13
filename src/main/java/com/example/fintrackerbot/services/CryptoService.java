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
public class CryptoService extends ViewPriceService {

    @Value("${crypto.api.url}")
    private String apiUrl;

    private final DecimalFormat df = new DecimalFormat("#,##0.00");

    public CryptoService(RestTemplate restTemplate) {
        super(restTemplate);
    }

    public Map<String, Double> getCryptoRates() throws Exception {
        JsonNode root = getJsonFromUrl(apiUrl);

        Map<String, Double> rates = new LinkedHashMap<>();
        for (String coin : List.of("bitcoin", "ethereum", "tether", "solana", "toncoin")) {
            rates.put(coin.toUpperCase(), root.path(coin).path("usd").asDouble());
        }
        return rates;
    }

    public String formatCryptoRates(Map<String, Double> rates) {
        return formatTable(rates, "Курсы криптовалют в USD:", "Валюта", "Цена (USD)", df);
    }
}
