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
public class CryptoService {
    @Value("${crypto.api.url}")
    private String apiUrl;

    private final RestTemplate restTemplate;
    private final DecimalFormat df = new DecimalFormat("#,##0.00");

    public CryptoService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public Map<String, Double> getCryptoRates() throws Exception {
        String response = restTemplate.getForObject(apiUrl, String.class);
        JsonNode rootNode = new ObjectMapper().readTree(response);

        Map<String, Double> rates = new LinkedHashMap<>();
        rates.put("BITCOIN", rootNode.path("bitcoin").path("usd").asDouble());
        rates.put("ETHEREUM", rootNode.path("ethereum").path("usd").asDouble());
        rates.put("TETHER", rootNode.path("tether").path("usd").asDouble());
        rates.put("SOLANA", rootNode.path("solana").path("usd").asDouble());
        rates.put("TONCOIN", rootNode.path("toncoin").path("usd").asDouble());

        return rates;
    }

    public String formatCryptoRates(Map<String, Double> rates) {
        StringBuilder sb = new StringBuilder("Курсы криптовалют в USD:\n \n");
        sb.append(String.format("%-10s | %12s%n", "Валюта", "Цена (USD)"));
        sb.append("------------+-------------\n");

        rates.forEach((k, v) -> sb.append(String.format("%-10s | %12s%n", k, df.format(v))));

        return sb.toString();
    }
}
