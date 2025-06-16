package com.example.fintrackerbot;

import com.example.fintrackerbot.services.CryptoService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

class CryptoServiceTest {

    private RestTemplate restTemplate;
    private CryptoService cryptoService;

    private final String jsonResponse = """
        {
            "bitcoin": {"usd": 30000.25},
            "ethereum": {"usd": 2000.75},
            "tether": {"usd": 1.00},
            "solana": {"usd": 35.50},
            "toncoin": {"usd": 6.10}
        }
        """;

    @BeforeEach
    void setUp() {
        restTemplate = mock(RestTemplate.class);
        cryptoService = new CryptoService(restTemplate);
        // Устанавливаем поле apiUrl через reflection, так как @Value не срабатывает вне Spring Context
        try {
            var field = CryptoService.class.getDeclaredField("apiUrl");
            field.setAccessible(true);
            field.set(cryptoService, "https://fake-api.com/crypto");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testGetCryptoRates() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(jsonResponse);

        // Мокаем метод getJsonFromUrl у родительского класса MarketQuoteService
        CryptoService spyService = Mockito.spy(cryptoService);
        doReturn(jsonNode).when(spyService).getJsonFromUrl("https://fake-api.com/crypto");

        Map<String, Double> rates = spyService.getCryptoRates();

        Map<String, Double> expected = new LinkedHashMap<>();
        expected.put("BITCOIN", 30000.25);
        expected.put("ETHEREUM", 2000.75);
        expected.put("TETHER", 1.00);
        expected.put("SOLANA", 35.50);
        expected.put("TONCOIN", 6.10);

        assertEquals(expected, rates);
    }

    @Test
    void testFormatCryptoRates() {
        Map<String, Double> inputRates = new LinkedHashMap<>();
        inputRates.put("BITCOIN", 30000.25);
        inputRates.put("ETHEREUM", 2000.75);

        String output = cryptoService.formatCryptoRates(inputRates);

        assertTrue(output.contains("BITCOIN"));
        assertTrue(output.contains("30,000.25"));
        assertTrue(output.contains("ETHEREUM"));
        assertTrue(output.contains("2,000.75"));
        assertTrue(output.contains("Курсы криптовалют в USD:"));
    }
}
