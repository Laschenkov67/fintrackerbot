package com.example.fintrackerbot;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.TimeUnit;

import com.example.fintrackerbot.services.CacheService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

public class CacheServiceTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private CacheService cacheService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void testPutToCache() {
        String key = "testKey";
        String value = "testValue";
        long ttl = 60L;

        cacheService.putToCache(key, value, ttl);

        verify(valueOperations, times(1)).set(key, value, ttl, TimeUnit.SECONDS);
    }

    @Test
    void testGetFromCache() {
        String key = "testKey";
        String expectedValue = "cachedValue";

        when(valueOperations.get(key)).thenReturn(expectedValue);

        String result = cacheService.getFromCache(key);

        assertEquals(expectedValue, result);
        verify(valueOperations, times(1)).get(key);
    }

    @Test
    void testEvictFromCache() {
        String key = "testKey";

        cacheService.evictFromCache(key);

        verify(redisTemplate, times(1)).delete(key);
    }
}
