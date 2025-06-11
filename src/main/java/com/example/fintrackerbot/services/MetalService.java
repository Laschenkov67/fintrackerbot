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
    private String apiUrl;

    private final RestTemplate restTemplate;
    private final DecimalFormat df = new DecimalFormat("#.##");

    public MetalService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public Map<String, Double> getMetalPrices() throws Exception {
        String response = restTemplate.getForObject(apiUrl, String.class);
        JsonNode rootNode = new ObjectMapper().readTree(response);

        Map<String, Double> prices = new LinkedHashMap<>();
        prices.put("Gold", rootNode.path("rates").path("XAU").asDouble());
        prices.put("Silver", rootNode.path("rates").path("XAG").asDouble());
        prices.put("Platinum", rootNode.path("rates").path("XPT").asDouble());

        return prices;
    }

    public String formatMetalPrices(Map<String, Double> prices) {
        StringBuilder sb = new StringBuilder("Цены на металлы (USD/унция):\n```\n");
        sb.append(String.format("%-10s | %10s%n", "Металл", "Цена"));
        sb.append("------------+------------\n");

        prices.forEach((k, v) -> sb.append(String.format("%-10s | %10s%n", k, df.format(v))));
        sb.append("```");

        return sb.toString();
    }
}
