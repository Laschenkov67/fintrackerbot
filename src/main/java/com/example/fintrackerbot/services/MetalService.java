package com.example.fintrackerbot.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;

import java.text.DecimalFormat;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class MetalService extends MarketQuoteService {

    @Value("${metal.api.url}")
    private String apiUrl;

    private final DecimalFormat df = new DecimalFormat("#.##");

    public MetalService(RestTemplate restTemplate) {
        super(restTemplate);
    }

    public Map<String, Double> getMetalPrices() throws Exception {
        JsonNode rootNode = getJsonFromUrl(apiUrl);
        if (!"success".equals(rootNode.path("status").asText())) {
            throw new RuntimeException("Ошибка при получении данных");
        }

        JsonNode metalsNode = rootNode.path("metals");
        Map<String, Double> prices = new LinkedHashMap<>();
        prices.put("Gold", metalsNode.path("gold").asDouble());
        prices.put("Silver", metalsNode.path("silver").asDouble());
        prices.put("Platinum", metalsNode.path("platinum").asDouble());

        return prices;
    }

    public String formatMetalPrices(Map<String, Double> prices) {
        return formatTable(prices, "Цены на металлы (USD/унция):", "Металл", "Цена", df);
    }
}
