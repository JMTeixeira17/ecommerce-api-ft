package com.farmatodo.ecommerce.application.usecase;

import com.farmatodo.ecommerce.application.dto.TokenizeRequest;
import com.farmatodo.ecommerce.application.dto.TokenizeResponse;
import com.farmatodo.ecommerce.domain.exception.InvalidCardDataException;
import com.farmatodo.ecommerce.domain.exception.TokenizationException;
import com.farmatodo.ecommerce.domain.port.out.SystemConfigRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TokenizeCardUseCaseImplTest {

    @Mock
    private SystemConfigRepositoryPort systemConfigRepository;

    @InjectMocks
    private TokenizeCardUseCaseImpl tokenizeCardUseCase;

    private TokenizeRequest validRequest;

    @BeforeEach
    void setUp() {
        validRequest = new TokenizeRequest();
        validRequest.setCardNumber("4242424242424242");
        validRequest.setCvv("123");
        validRequest.setCardHolderName("Jose Teixeira");
        LocalDate nextYear = LocalDate.now().plusYears(1);
        validRequest.setExpirationMonth(String.format("%02d", nextYear.getMonthValue()));
        validRequest.setExpirationYear(String.valueOf(nextYear.getYear()));
    }
    @Test
    void whenTokenize_withValidCard_andNoRejection_shouldSucceed() {
        when(systemConfigRepository.getValueAsDouble(anyString(), anyDouble())).thenReturn(0.0);
        TokenizeResponse response = tokenizeCardUseCase.tokenize(validRequest);
        assertNotNull(response);
        assertNotNull(response.getToken());
        assertTrue(response.getToken().startsWith("tkn_"));
        assertEquals("VISA", response.getCardBrand());
        assertEquals("4242", response.getLastFourDigits());
    }

    @Test
    void whenTokenize_isRejectedByProbability_shouldThrowTokenizationException() {
        when(systemConfigRepository.getValueAsDouble(anyString(), anyDouble())).thenReturn(1.0);
        Exception exception = assertThrows(TokenizationException.class, () -> {
            tokenizeCardUseCase.tokenize(validRequest);
        });
        assertEquals("Tokenizacion rechazada por el proveedor", exception.getMessage());
    }

    @Test
    void whenTokenize_withInvalidLuhn_shouldThrowInvalidCardDataException() {
        validRequest.setCardNumber("4242424242424241");
        Exception exception = assertThrows(InvalidCardDataException.class, () -> {
            tokenizeCardUseCase.tokenize(validRequest);
        });
        assertEquals("Numero de tarjeta inválido (Luhn check failed)", exception.getMessage());
    }

    @Test
    void whenTokenize_withExpiredCard_shouldThrowInvalidCardDataException() {
        LocalDate lastYear = LocalDate.now().minusYears(1);
        validRequest.setExpirationMonth(String.format("%02d", lastYear.getMonthValue()));
        validRequest.setExpirationYear(String.valueOf(lastYear.getYear()));
        Exception exception = assertThrows(InvalidCardDataException.class, () -> {
            tokenizeCardUseCase.tokenize(validRequest);
        });
        assertEquals("Tarjeta está expirada", exception.getMessage());
    }

    @Test
    void whenTokenize_withInvalidMonth_shouldThrowInvalidCardDataException() {
        validRequest.setExpirationMonth("13");
        Exception exception = assertThrows(InvalidCardDataException.class, () -> {
            tokenizeCardUseCase.tokenize(validRequest);
        });
        assertEquals("Mes de expiración inválido", exception.getMessage());
    }

    @Test
    void whenTokenize_withInvalidCVV_shouldThrowInvalidCardDataException() {
        validRequest.setCvv("12345");
        Exception exception = assertThrows(InvalidCardDataException.class, () -> {
            tokenizeCardUseCase.tokenize(validRequest);
        });
        assertEquals("CVV inválido", exception.getMessage());
    }

    @Test
    void whenTokenize_withUnsupportedBrand_shouldThrowInvalidCardDataException() {
        validRequest.setCardNumber("1234567890123456");
        Exception exception = assertThrows(InvalidCardDataException.class, () -> {
            tokenizeCardUseCase.tokenize(validRequest);
        });
        assertEquals("Numero de tarjeta inválido (Luhn check failed)", exception.getMessage());
    }

    @Test
    void whenTokenize_withNonNumericExpirationDate_shouldThrowInvalidCardDataException() {
        validRequest.setExpirationMonth("AB");
        validRequest.setExpirationYear("2029");
        Exception exception = assertThrows(InvalidCardDataException.class, () -> {
            tokenizeCardUseCase.tokenize(validRequest);
        });
        assertEquals("El mes o año de expiración no son un número", exception.getMessage());
    }

}