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
public class CurrencyService {
    @Value("${currency.api.url}")
    private String apiUrl;

    private final RestTemplate restTemplate;
    private final DecimalFormat df = new DecimalFormat("#.####");

    public CurrencyService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public Map<String, Double> getCurrencyRates() throws Exception {
        String response = restTemplate.getForObject(apiUrl, String.class);
        JsonNode rootNode = new ObjectMapper().readTree(response);
        JsonNode ratesNode = rootNode.path("rates");

        Map<String, Double> rates = new LinkedHashMap<>();
        rates.put("USD", 1 / ratesNode.path("USD").asDouble());
        rates.put("EUR", 1 / ratesNode.path("EUR").asDouble());
        rates.put("JPY", 1 / ratesNode.path("JPY").asDouble());
        rates.put("CNY", 1 / ratesNode.path("CNY").asDouble());
        rates.put("GBP", 1 / ratesNode.path("GBP").asDouble());

        return rates;
    }

    public String formatCurrencyRates(Map<String, Double> rates) {
        StringBuilder sb = new StringBuilder("Курсы валют к RUB:\n \n");
        sb.append(String.format("%-6s | %10s%n", "Валюта", "Курс"));
        sb.append("--------+------------\n");

        rates.forEach((k, v) -> sb.append(String.format("%-6s | %10s%n", k, df.format(v))));

        return sb.toString();
    }
}
