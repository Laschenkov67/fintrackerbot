package com.example.fintrackerbot.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.text.DecimalFormat;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class MetalService {
    @Value("${metal.api.url}")
    private String apiUrl; // например: https://api.metals.dev/v1/latest?currency=USD

    private final RestTemplate restTemplate;
    private final DecimalFormat df = new DecimalFormat("#.##");

    public MetalService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public Map<String, Double> getMetalPrices() throws Exception {
        String response = restTemplate.getForObject(apiUrl, String.class);
        JsonNode rootNode = new ObjectMapper().readTree(response);

        if (!rootNode.path("status").asText().equals("success")) {
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
        StringBuilder sb = new StringBuilder("Цены на металлы (USD/унция):\n \n");
        sb.append(String.format("%-10s | %10s%n", "Металл", "Цена"));
        sb.append("------------+------------\n");

        prices.forEach((metal, price) ->
                sb.append(String.format("%-10s | %10.2f%n", metal, price))
        );

        return sb.toString();
    }
}
