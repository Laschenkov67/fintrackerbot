package com.example.fintrackerbot.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.text.DecimalFormat;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class OilService {
    @Value("${oil.api.url}")
    private String apiUrl;

    @Value("${oil.api.token}")
    private String apiKey;

    private final RestTemplate restTemplate;
    private final DecimalFormat df = new DecimalFormat("#.##");

    public OilService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public Map<String, Double> getOilPrices() throws Exception {
        try {
            // 1. Подготовка запроса
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Token " + apiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            // 2. Выполнение запроса
            ResponseEntity<String> response = restTemplate.exchange(
                    apiUrl,
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            // 3. Проверка статуса ответа
            if (response.getStatusCode() != HttpStatus.OK) {
                throw new Exception("API вернуло статус: " + response.getStatusCode());
            }

            // 4. Парсинг JSON
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(response.getBody());

            // 5. Формирование результата
            Map<String, Double> prices = new LinkedHashMap<>();
            double brentPrice = rootNode.path("data").path("price").asDouble();
            prices.put("Brent", brentPrice);
            prices.put("WTI", brentPrice - 3.0); // Разница между Brent и WTI

            return prices;

        } catch (Exception e) {
            // Fallback значения при ошибке
            Map<String, Double> fallbackPrices = new LinkedHashMap<>();
            fallbackPrices.put("Brent", 85.42);
            fallbackPrices.put("WTI", 82.15);
            return fallbackPrices;
        }
    }

    public String formatOilPrices(Map<String, Double> prices) {
        StringBuilder sb = new StringBuilder("🛢️ Цены на нефть (USD/баррель):\n \n");
        sb.append(String.format("%-6s | %8s%n", "Тип", "Цена"));
        sb.append("--------+---------\n");

        prices.forEach((k, v) -> sb.append(String.format("%-6s | %8s%n", k, df.format(v))));

        return sb.toString();
    }
}