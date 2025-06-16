package com.example.fintrackerbot;
import com.example.fintrackerbot.services.OilService;
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

class OilServiceTest {

    private RestTemplate restTemplate;
    private OilService oilService;

    private final String validJsonResponse = """
        {
            "data": {
                "price": 88.75
            }
        }
        """;

    @BeforeEach
    void setUp() {
        restTemplate = mock(RestTemplate.class);
        oilService = new OilService(restTemplate);

        // Установка значений apiUrl и apiKey через reflection
        try {
            var urlField = OilService.class.getDeclaredField("apiUrl");
            urlField.setAccessible(true);
            urlField.set(oilService, "https://fake-api.com/oil");

            var keyField = OilService.class.getDeclaredField("apiKey");
            keyField.setAccessible(true);
            keyField.set(oilService, "dummy-token");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testGetOilPrices_success() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(validJsonResponse);

        OilService spyService = Mockito.spy(oilService);
        doReturn(jsonNode).when(spyService).getJsonFromUrlWithAuth("https://fake-api.com/oil", "dummy-token");

        Map<String, Double> actual = spyService.getOilPrices();

        Map<String, Double> expected = new LinkedHashMap<>();
        expected.put("Brent", 88.75);
        expected.put("WTI", 85.75); // 88.75 - 3

        assertEquals(expected, actual);
    }

    @Test
    void testGetOilPrices_fallbackOnException() {
        OilService spyService = Mockito.spy(oilService);

        try {
            doThrow(new RuntimeException("API error")).when(spyService)
                    .getJsonFromUrlWithAuth("https://fake-api.com/oil", "dummy-token");
        } catch (Exception e) {
            fail("Mock setup failed");
        }

        Map<String, Double> fallback = spyService.getOilPrices();

        assertEquals(85.42, fallback.get("Brent"));
        assertEquals(82.15, fallback.get("WTI"));
    }

    @Test
    void testFormatOilPrices() {
        Map<String, Double> prices = new LinkedHashMap<>();
        prices.put("Brent", 88.75);
        prices.put("WTI", 85.75);

        String output = oilService.formatOilPrices(prices);

        assertTrue(output.contains("🛢️ Цены на нефть (USD/баррель):"));
        assertTrue(output.contains("Brent"));
        assertTrue(output.contains("88.75"));
        assertTrue(output.contains("WTI"));
        assertTrue(output.contains("85.75"));
    }
}
