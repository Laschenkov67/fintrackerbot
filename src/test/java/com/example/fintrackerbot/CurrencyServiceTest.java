package com.example.fintrackerbot;

import com.example.fintrackerbot.services.CurrencyService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CurrencyServiceTest {

    private RestTemplate restTemplate;
    private CurrencyService currencyService;

    private final String jsonResponse = """
        {
            "rates": {
                "USD": 89.0,
                "EUR": 96.5,
                "JPY": 0.62,
                "CNY": 12.3,
                "GBP": 112.0
            }
        }
        """;

    @BeforeEach
    void setUp() {
        restTemplate = mock(RestTemplate.class);
        currencyService = new CurrencyService(restTemplate);

        // Устанавливаем значение apiUrl вручную
        try {
            var field = CurrencyService.class.getDeclaredField("apiUrl");
            field.setAccessible(true);
            field.set(currencyService, "https://fake-api.com/currency");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testGetCurrencyRates() throws Exception {
        // Подготовка JSON
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(jsonResponse);

        // Создание spy и мока метода getJsonFromUrl
        CurrencyService spyService = Mockito.spy(currencyService);
        doReturn(jsonNode).when(spyService).getJsonFromUrl("https://fake-api.com/currency");

        // Выполнение
        Map<String, Double> actualRates = spyService.getCurrencyRates();

        // Ожидаемый результат
        Map<String, Double> expectedRates = new LinkedHashMap<>();
        expectedRates.put("USD", 1 / 89.0);
        expectedRates.put("EUR", 1 / 96.5);
        expectedRates.put("JPY", 1 / 0.62);
        expectedRates.put("CNY", 1 / 12.3);
        expectedRates.put("GBP", 1 / 112.0);

        // Проверка
        assertEquals(expectedRates.size(), actualRates.size());
        expectedRates.forEach((currency, expectedRate) -> {
            assertTrue(actualRates.containsKey(currency));
            assertEquals(expectedRate, actualRates.get(currency), 0.0001);
        });
    }

    @Test
    void testFormatCurrencyRates() {
        Map<String, Double> inputRates = new LinkedHashMap<>();
        inputRates.put("USD", 0.0112);
        inputRates.put("EUR", 0.0103);

        String output = currencyService.formatCurrencyRates(inputRates);

        assertTrue(output.contains("Курсы валют к RUB:"));
        assertTrue(output.contains("USD"));
        assertTrue(output.contains("0.0112"));
        assertTrue(output.contains("EUR"));
        assertTrue(output.contains("0.0103"));
    }
}
