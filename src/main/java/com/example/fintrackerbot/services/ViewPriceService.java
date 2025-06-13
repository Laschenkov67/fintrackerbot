package com.example.fintrackerbot.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Map;

public abstract class ViewPriceService {

    protected final RestTemplate restTemplate;
    protected final ObjectMapper objectMapper = new ObjectMapper();

    protected ViewPriceService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    protected JsonNode getJsonFromUrl(String url) throws IOException {
        String response = restTemplate.getForObject(url, String.class);
        return objectMapper.readTree(response);
    }

    protected JsonNode getJsonFromUrlWithAuth(String url, String token) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Token " + token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        if (response.getStatusCode() != HttpStatus.OK) {
            throw new IOException("API вернуло статус: " + response.getStatusCode());
        }
        return objectMapper.readTree(response.getBody());
    }

    protected String formatTable(Map<String, Double> data, String title, String keyLabel, String valueLabel, DecimalFormat format) {
        StringBuilder sb = new StringBuilder(title + "\n\n");
        sb.append(String.format("%-12s | %12s%n", keyLabel, valueLabel));
        sb.append("--------------+--------------\n");

        data.forEach((k, v) ->
                sb.append(String.format("%-12s | %12s%n", k, format.format(v)))
        );
        return sb.toString();
    }
}
