package com.example.fintrackerbot.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Map;
import java.util.function.Supplier;

public abstract class MarketQuoteService {

    protected final RestTemplate restTemplate;
    protected final ObjectMapper objectMapper;

    protected MarketQuoteService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Выполняет HTTP GET-запрос и возвращает корневой JSON-узел.
     */
    public JsonNode getJsonFromUrl(String url) throws IOException {
        String response = restTemplate.getForObject(url, String.class);
        if (response == null || response.isEmpty()) {
            throw new IOException("Пустой ответ от API: " + url);
        }
        return objectMapper.readTree(response);
    }

    /**
     * Выполняет HTTP GET-запрос с авторизацией по токену и возвращает JSON.
     */
    public JsonNode getJsonFromUrlWithAuth(String url, String token) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new IOException("API вернул ошибку: " + response.getStatusCode());
        }

        String body = response.getBody();
        if (body == null || body.isEmpty()) {
            throw new IOException("Пустое тело ответа от API: " + url);
        }

        return objectMapper.readTree(body);
    }

    /**
     * Форматирует данные в табличный текст с заголовком и колонками.
     */
    protected String formatTable(
            Map<String, Double> data,
            String title,
            String keyHeader,
            String valueHeader,
            DecimalFormat format
    ) {
        int keyWidth = Math.max(keyHeader.length(), data.keySet().stream().mapToInt(String::length).max().orElse(10));
        int valWidth = Math.max(valueHeader.length(), 12);

        StringBuilder sb = new StringBuilder(title + "\n\n");
        String formatLine = "%-" + keyWidth + "s | %" + valWidth + "s%n";
        String separator = "-".repeat(keyWidth) + "-+-" + "-".repeat(valWidth) + "\n";

        sb.append(String.format(formatLine, keyHeader, valueHeader));
        sb.append(separator);

        data.forEach((k, v) -> sb.append(String.format(formatLine, k, format.format(v))));

        return sb.toString();
    }

    /**
     * Обёртка для безопасного вызова API с fallback-результатом.
     */
    protected Map<String, Double> safeFetch(Supplier<Map<String, Double>> fetcher, Map<String, Double> fallback) {
        try {
            return fetcher.get();
        } catch (Exception ex) {
            return fallback;
        }
    }
}
