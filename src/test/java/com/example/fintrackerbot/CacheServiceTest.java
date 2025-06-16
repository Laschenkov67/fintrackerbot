package com.example.fintrackerbot;

import com.example.fintrackerbot.services.CacheService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CacheServiceTest {

    private RedisTemplate<String, Object> redisTemplate;
    private ValueOperations<String, Object> valueOperations;
    private CacheService cacheService;

    @BeforeEach
    void setUp() {
        redisTemplate = mock(RedisTemplate.class);
        valueOperations = mock(ValueOperations.class);

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        cacheService = new CacheService(redisTemplate);
    }

    @Test
    void testPut() {
        cacheService.put("key1", "value1", 10, TimeUnit.MINUTES);
        verify(valueOperations).set("key1", "value1", 10, TimeUnit.MINUTES);
    }

    @Test
    void testGet_existingValue() {
        when(valueOperations.get("key1")).thenReturn("value1");

        String result = cacheService.get("key1", String.class);
        assertEquals("value1", result);
    }

    @Test
    void testGet_wrongType() {
        when(valueOperations.get("key1")).thenReturn(123); // Integer stored, expecting String

        String result = cacheService.get("key1", String.class);
        assertNull(result);
    }

    @Test
    void testGet_nullValue() {
        when(valueOperations.get("missing")).thenReturn(null);

        String result = cacheService.get("missing", String.class);
        assertNull(result);
    }

    @Test
    void testDelete() {
        cacheService.delete("key1");
        verify(redisTemplate).delete("key1");
    }

    @Test
    void testHasKey_true() {
        when(redisTemplate.hasKey("key1")).thenReturn(true);
        assertTrue(cacheService.hasKey("key1"));
    }

    @Test
    void testHasKey_false() {
        when(redisTemplate.hasKey("key1")).thenReturn(false);
        assertFalse(cacheService.hasKey("key1"));
    }

    @Test
    void testHasKey_null() {
        when(redisTemplate.hasKey("key1")).thenReturn(null);
        assertFalse(cacheService.hasKey("key1")); // null treated as false
    }
}
