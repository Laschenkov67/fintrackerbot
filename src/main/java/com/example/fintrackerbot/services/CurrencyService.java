package com.example.fintrackerbot.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.text.DecimalFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class CurrencyService {
    private static final String CURRENCY_RATES_CACHE_KEY = "currency_rates";

    private static final long CACHE_TTL_MINUTES = 30;

    @Value("${currency.api.url}")
    private String apiUrl;

    private final RestTemplate restTemplate;
    private final DecimalFormat df = new DecimalFormat("#.####");
    private final CacheService cacheService;
    private final LoggingService loggingService;

    public CurrencyService(RestTemplate restTemplate,
                           CacheService cacheService,
                           LoggingService loggingService) {
        this.restTemplate = restTemplate;
        this.cacheService = cacheService;
        this.loggingService = loggingService;

        loggingService.logInfo("CurrencyService initialized");
        loggingService.logDebug("Currency API URL: " + apiUrl);
    }

    public Map<String, Double> getCurrencyRates() throws Exception {
        loggingService.logDebug("Attempting to get currency rates. Checking cache first...");

        Map<String, Double> cachedRates = cacheService.get(CURRENCY_RATES_CACHE_KEY, Map.class);
        if (cachedRates != null) {
            loggingService.logInfo("Currency rates successfully retrieved from cache");
            loggingService.logDebug("Cached rates: " + cachedRates);
            return cachedRates;
        }

        loggingService.logInfo("Cache miss. Fetching currency rates from external API: " + apiUrl);

        try {
            long startTime = System.currentTimeMillis();
            String response = restTemplate.getForObject(apiUrl, String.class);

            long duration = System.currentTimeMillis() - startTime;
            loggingService.logDebug("API request completed in " + duration + "ms");
            loggingService.logTrace("Raw API response: " + response);

            JsonNode rootNode = new ObjectMapper().readTree(response);
            JsonNode ratesNode = rootNode.path("rates");

            Map<String, Double> rates = new LinkedHashMap<>();
            // Добавляем логирование каждого курса
            rates.put("USD", logAndGetRate(ratesNode, "USD"));
            rates.put("EUR", logAndGetRate(ratesNode, "EUR"));
            rates.put("JPY", logAndGetRate(ratesNode, "JPY"));
            rates.put("CNY", logAndGetRate(ratesNode, "CNY"));
            rates.put("GBP", logAndGetRate(ratesNode, "GBP"));

            cacheService.put(CURRENCY_RATES_CACHE_KEY, rates, CACHE_TTL_MINUTES, TimeUnit.MINUTES);
            loggingService.logInfo("Currency rates successfully cached for " + CACHE_TTL_MINUTES + " minutes");
            loggingService.logDebug("Cached rates: " + rates);

            return rates;
        } catch (Exception e) {
            loggingService.logError("Failed to fetch and parse currency rates from API", e);
            throw new Exception("Ошибка при получении курсов валют: " + e.getMessage(), e);
        }
    }

    private double logAndGetRate(JsonNode ratesNode, String currency) {
        double rate = 1 / ratesNode.path(currency).asDouble();
        loggingService.logTrace("Processed " + currency + " rate: " + rate);
        return rate;
    }

    public String formatCurrencyRates(Map<String, Double> rates) {
        loggingService.logDebug("Starting to format currency rates for display");

        if (rates == null || rates.isEmpty()) {
            loggingService.logWarn("Empty or null rates map provided for formatting");
            return "Нет данных о курсах валют";
        }

        try {
            StringBuilder sb = new StringBuilder("Курсы валют к RUB:\n \n");
            sb.append(String.format("%-6s | %10s%n", "Валюта", "Курс"));
            sb.append("--------+------------\n");

            rates.forEach((currency, rate) -> {
                String formattedRate = df.format(rate);
                sb.append(String.format("%-6s | %10s%n", currency, formattedRate));
                loggingService.logTrace("Formatted rate: " + currency + " - " + formattedRate);
            });

            String result = sb.toString();
            loggingService.logDebug("Successfully formatted currency rates");
            loggingService.logTrace("Formatted result:\n" + result);

            return result;
        } catch (Exception e) {
            loggingService.logError("Error while formatting currency rates", e);
            return "Ошибка при форматировании курсов валют";
        }
    }

    public void clearCurrencyRatesCache() {
        try {
            cacheService.delete(CURRENCY_RATES_CACHE_KEY);
            loggingService.logInfo("Currency rates cache successfully cleared");
        } catch (Exception e) {
            loggingService.logError("Failed to clear currency rates cache", e);
        }
    }
}
