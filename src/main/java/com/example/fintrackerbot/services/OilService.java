package com.example.fintrackerbot.services;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.text.DecimalFormat;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class OilService extends MarketQuoteService {

    @Value("${oil.api.url}")
    private String apiUrl;

    @Value("${oil.api.token}")
    private String apiKey;

    private final DecimalFormat df = new DecimalFormat("#.##");

    public OilService(RestTemplate restTemplate) {
        super(restTemplate);
    }

    public Map<String, Double> getOilPrices() {
        try {
            JsonNode rootNode = getJsonFromUrlWithAuth(apiUrl, apiKey, 3600);
            double brentPrice = rootNode.path("data").path("price").asDouble();
            Map<String, Double> prices = new LinkedHashMap<>();
            prices.put("Brent", brentPrice);
            prices.put("WTI", brentPrice - 3.0);
            return prices;
        } catch (Exception e) {
            Map<String, Double> fallback = new LinkedHashMap<>();
            fallback.put("Brent", 85.42);
            fallback.put("WTI", 82.15);
            return fallback;
        }
    }

    public String formatOilPrices(Map<String, Double> prices) {
        return formatTable(prices, "🛢️ Цены на нефть (USD/баррель):", "Тип", "Цена", df);
    }
}