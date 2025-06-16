package com.example.fintrackerbot;
import com.example.fintrackerbot.services.MetalService;
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

class MetalServiceTest {

    private RestTemplate restTemplate;
    private MetalService metalService;

    private final String validJsonResponse = """
        {
            "status": "success",
            "metals": {
                "gold": 2334.50,
                "silver": 29.12,
                "platinum": 978.30
            }
        }
        """;

    private final String failedJsonResponse = """
        {
            "status": "error",
            "metals": {}
        }
        """;

    @BeforeEach
    void setUp() {
        restTemplate = mock(RestTemplate.class);
        metalService = new MetalService(restTemplate);

        // Установка значения apiUrl вручную
        try {
            var field = MetalService.class.getDeclaredField("apiUrl");
            field.setAccessible(true);
            field.set(metalService, "https://fake-api.com/metals");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testGetMetalPrices_success() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(validJsonResponse);

        MetalService spyService = Mockito.spy(metalService);
        doReturn(jsonNode).when(spyService).getJsonFromUrl("https://fake-api.com/metals");

        Map<String, Double> actualPrices = spyService.getMetalPrices();

        Map<String, Double> expectedPrices = new LinkedHashMap<>();
        expectedPrices.put("Gold", 2334.50);
        expectedPrices.put("Silver", 29.12);
        expectedPrices.put("Platinum", 978.30);

        assertEquals(expectedPrices, actualPrices);
    }

    @Test
    void testGetMetalPrices_failureStatus() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(failedJsonResponse);

        MetalService spyService = Mockito.spy(metalService);
        doReturn(jsonNode).when(spyService).getJsonFromUrl("https://fake-api.com/metals");

        RuntimeException ex = assertThrows(RuntimeException.class, spyService::getMetalPrices);
        assertEquals("Ошибка при получении данных", ex.getMessage());
    }

    @Test
    void testFormatMetalPrices() {
        Map<String, Double> prices = new LinkedHashMap<>();
        prices.put("Gold", 2334.50);
        prices.put("Silver", 29.12);
        prices.put("Platinum", 978.30);

        String output = metalService.formatMetalPrices(prices);

        assertTrue(output.contains("Цены на металлы (USD/унция):"));
        assertTrue(output.contains("Gold"));
        assertTrue(output.contains("2334.5"));
        assertTrue(output.contains("Silver"));
        assertTrue(output.contains("29.12"));
        assertTrue(output.contains("Platinum"));
    }
}