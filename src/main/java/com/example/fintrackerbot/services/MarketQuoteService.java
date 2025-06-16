package com.example.fintrackerbot.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Map;
import java.util.function.Supplier;

public abstract class MarketQuoteService {

    protected final RestTemplate restTemplate;
    protected final ObjectMapper objectMapper;

    @Autowired
    protected CacheService cacheService;

    @Autowired
    protected LoggingService loggingService;

    protected MarketQuoteService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Выполняет HTTP GET-запрос с кешированием.
     */
    public JsonNode getJsonFromUrl(String url, long ttlSeconds) throws IOException {
        loggingService.logDebug("Попытка получить данные с URL: " + url);

        if (cacheService.exists(url)) {
            loggingService.logInfo("Ответ получен из кеша: " + url);
            Object cached = cacheService.get(url);
            return objectMapper.readTree(cached.toString());
        }

        loggingService.logInfo("Кеш отсутствует, выполняется HTTP GET: " + url);
        String response = restTemplate.getForObject(url, String.class);

        if (response == null || response.isEmpty()) {
            loggingService.logError("Пустой ответ от API: " + url);
            throw new IOException("Пустой ответ от API: " + url);
        }

        cacheService.set(url, response, ttlSeconds);
        loggingService.logDebug("Результат сохранён в кеш: " + url);
        return objectMapper.readTree(response);
    }

    /**
     * Выполняет HTTP GET-запрос с авторизацией и кешированием.
     */
    public JsonNode getJsonFromUrlWithAuth(String url, String token, long ttlSeconds) throws IOException {
        String cacheKey = url + "::" + token.hashCode();
        loggingService.logDebug("Проверка кеша с ключом: " + cacheKey);

        if (cacheService.exists(cacheKey)) {
            loggingService.logInfo("Ответ из кеша по авторизованному запросу: " + url);
            Object cached = cacheService.get(cacheKey);
            return objectMapper.readTree(cached.toString());
        }

        loggingService.logInfo("Кеш отсутствует, выполняется авторизованный HTTP GET: " + url);
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            loggingService.logError("API вернул ошибку: " + response.getStatusCode());
            throw new IOException("API вернул ошибку: " + response.getStatusCode());
        }

        String body = response.getBody();
        if (body == null || body.isEmpty()) {
            loggingService.logError("Пустое тело ответа от API: " + url);
            throw new IOException("Пустое тело ответа от API: " + url);
        }

        cacheService.set(cacheKey, body, ttlSeconds);
        loggingService.logDebug("Авторизованный ответ сохранён в кеш");
        return objectMapper.readTree(body);
    }

    /**
     * Форматирует Map в табличный текст.
     */
    protected String formatTable(
            Map<String, Double> data,
            String title,
            String keyHeader,
            String valueHeader,
            DecimalFormat format
    ) {
        loggingService.logDebug("Форматирование данных в таблицу");
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
     * Безопасный fetch с fallback и логированием.
     */
    protected Map<String, Double> safeFetch(Supplier<Map<String, Double>> fetcher, Map<String, Double> fallback) {
        try {
            loggingService.logDebug("Попытка безопасного fetch API");
            return fetcher.get();
        } catch (Exception ex) {
            loggingService.logError("Ошибка при fetch API", ex);
            return fallback;
        }
    }
}
