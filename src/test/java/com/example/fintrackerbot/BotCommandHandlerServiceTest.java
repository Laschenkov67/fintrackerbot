package com.example.fintrackerbot;

import com.example.fintrackerbot.services.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BotCommandHandlerServiceTest {

    @Mock
    private CurrencyService currencyService;

    @Mock
    private CryptoService cryptoService;

    @Mock
    private MetalService metalService;

    @Mock
    private OilService oilService;

    private BotCommandHandlerService handlerService;

    @BeforeEach
    void setUp() {
        handlerService = new BotCommandHandlerService(
                currencyService, cryptoService, metalService, oilService
        );
    }

    @Test
    void shouldReturnReplyKeyboardMessageForStartCommand() {
        String expected = "Выберите значение из списка:\n" +
                "Курсы валют | Курсы криптовалют\n" +
                "Драгоценные металлы | Нефть\n";

        String actual = handlerService.handleCommand("/start");

        assertEquals(expected, actual);
    }

    @Test
    void shouldReturnCurrencyRates() throws Exception {
        when(currencyService.getCurrencyRates()).thenReturn((Map<String, Double>) List.of(/* any DTO */));
        when(currencyService.formatCurrencyRates(any())).thenReturn("Курсы валют: 123");

        String actual = handlerService.handleCommand("Курсы валют");

        assertEquals("Курсы валют: 123", actual);
        verify(currencyService).getCurrencyRates();
        verify(currencyService).formatCurrencyRates(any());
    }

    @Test
    void shouldReturnCryptoRates() throws Exception {
        when(cryptoService.getCryptoRates()).thenReturn((Map<String, Double>) List.of(/* any DTO */));
        when(cryptoService.formatCryptoRates(any())).thenReturn("Крипта: OK");

        String actual = handlerService.handleCommand("Курсы криптовалют");

        assertEquals("Крипта: OK", actual);
    }

    @Test
    void shouldReturnMetalRates() throws Exception {
        when(metalService.getMetalPrices()).thenReturn((Map<String, Double>) List.of());
        when(metalService.formatMetalPrices(any())).thenReturn("Металлы: OK");

        String actual = handlerService.handleCommand("Драгоценные металлы");

        assertEquals("Металлы: OK", actual);
    }

    @Test
    void shouldReturnOilPrices() {
        when(oilService.getOilPrices()).thenReturn((Map<String, Double>) List.of());
        when(oilService.formatOilPrices(any())).thenReturn("Нефть: OK");

        String actual = handlerService.handleCommand("Нефть");

        assertEquals("Нефть: OK", actual);
    }

    @Test
    void shouldReturnErrorMessageOnCurrencyServiceException() throws Exception {
        when(currencyService.getCurrencyRates()).thenThrow(new RuntimeException("Ошибка"));

        String actual = handlerService.handleCommand("Курсы валют");

        assertEquals("Ошибка получения курсов валют", actual);
    }

    @Test
    void shouldReturnNullForUnknownCommand() {
        String actual = handlerService.handleCommand("Непонятная команда");
        assertNull(actual);
    }
}
