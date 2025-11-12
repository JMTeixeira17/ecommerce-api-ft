package com.farmatodo.ecommerce.application.usecase;

import com.farmatodo.ecommerce.application.dto.RegisterCardResponse;
import com.farmatodo.ecommerce.application.dto.TokenizeRequest;
import com.farmatodo.ecommerce.application.dto.TokenizeResponse;
import com.farmatodo.ecommerce.domain.exception.CardAlreadyExistsException;
import com.farmatodo.ecommerce.domain.exception.InvalidCardDataException;
import com.farmatodo.ecommerce.domain.exception.TokenizationException;
import com.farmatodo.ecommerce.domain.model.Customer;
import com.farmatodo.ecommerce.domain.model.TokenizedCard;
import com.farmatodo.ecommerce.domain.port.out.TokenizedCardRepositoryPort;
import com.farmatodo.ecommerce.infrastructure.common.ApiResponse;
import com.farmatodo.ecommerce.infrastructure.security.EncryptionService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterCardUseCaseImplTest {

    @Mock
    private RestTemplate restTemplate;
    @Mock
    private EncryptionService encryptionService;
    @Mock
    private TokenizedCardRepositoryPort cardRepositoryPort;
    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private RegisterCardUseCaseImpl registerCardUseCase;

    private TokenizeRequest tokenizeRequest;
    private Customer customer;
    private TokenizeResponse tokenizeResponse;
    private final String mockApiKey = "test_api_key_123";

    @BeforeEach
    void setUp() {
        customer = Customer.builder().id(1L).email("test@user.com").build();

        tokenizeRequest = new TokenizeRequest();
        tokenizeRequest.setCardNumber("4242424242424242");
        tokenizeRequest.setCardHolderName("Test User");
        tokenizeRequest.setExpirationMonth("12");
        tokenizeRequest.setExpirationYear("2029");
        tokenizeRequest.setCvv("123");

        tokenizeResponse = TokenizeResponse.builder()
                .token("tkn_123")
                .cardBrand("VISA")
                .lastFourDigits("4242")
                .build();
    }

    @Test
    void whenRegisterCard_withNewCard_shouldSucceed() {
        ApiResponse<TokenizeResponse> apiResponse = ApiResponse.success(tokenizeResponse);
        ResponseEntity<ApiResponse<TokenizeResponse>> responseEntity = new ResponseEntity<>(apiResponse, HttpStatus.OK);
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenReturn(responseEntity);
        when(cardRepositoryPort.existsByCustomerIdAndLastFourDigitsAndCardBrand(1L, "4242", "VISA"))
                .thenReturn(false);
        when(encryptionService.encrypt("12")).thenReturn("enc-month");
        when(encryptionService.encrypt("2029")).thenReturn("enc-year");
        TokenizedCard savedCard = TokenizedCard.builder()
                .id(100L)
                .uuid(UUID.randomUUID())
                .lastFourDigits("4242")
                .cardBrand("VISA")
                .build();
        when(cardRepositoryPort.save(any(TokenizedCard.class))).thenReturn(savedCard);
        RegisterCardResponse response = registerCardUseCase.registerCard(tokenizeRequest, customer, mockApiKey);
        assertNotNull(response);
        assertEquals("4242", response.getLastFourDigits());
        assertEquals("VISA", response.getCardBrand());
        verify(encryptionService, times(1)).encrypt("12");
        verify(cardRepositoryPort, times(1)).save(any(TokenizedCard.class));
    }

    @Test
    void whenRegisterCard_withExistingCard_shouldThrowCardAlreadyExistsException() {
        ApiResponse<TokenizeResponse> apiResponse = ApiResponse.success(tokenizeResponse);
        ResponseEntity<ApiResponse<TokenizeResponse>> responseEntity = new ResponseEntity<>(apiResponse, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), any(), any(), any(ParameterizedTypeReference.class)))
                .thenReturn(responseEntity);
        when(cardRepositoryPort.existsByCustomerIdAndLastFourDigitsAndCardBrand(1L, "4242", "VISA"))
                .thenReturn(true);
        Exception exception = assertThrows(CardAlreadyExistsException.class, () -> {
            registerCardUseCase.registerCard(tokenizeRequest, customer, mockApiKey);
        });
        assertTrue(exception.getMessage().contains("ya se encuentra registrada"));
        verify(cardRepositoryPort, never()).save(any(TokenizedCard.class));
    }

    @Test
    void whenTokenizationFails_withInvalidApiKey_shouldThrowIllegalArgumentException() throws JsonProcessingException {
        String errorBody = "{\"error\":\"APIKEY invalida. Por favor revise el formato.\"}";
        HttpClientErrorException exception = HttpClientErrorException.create(
                HttpStatus.FORBIDDEN, "Forbidden", null, errorBody.getBytes(StandardCharsets.UTF_8), null
        );
        when(restTemplate.exchange(anyString(), any(), any(), any(ParameterizedTypeReference.class)))
                .thenThrow(exception);
        when(objectMapper.readTree(errorBody)).thenReturn(new ObjectMapper().readTree(errorBody));
        Exception thrown = assertThrows(IllegalArgumentException.class, () -> {
            registerCardUseCase.registerCard(tokenizeRequest, customer, mockApiKey);
        });
        assertTrue(thrown.getMessage().contains("APIKEY invalida"));
        verify(cardRepositoryPort, never()).save(any(TokenizedCard.class));
    }

    @Test
    void whenTokenizationFails_withInvalidCardData_shouldThrowInvalidCardDataException() throws JsonProcessingException {
        String errorBody = "{\"error\":\"Datos de tarjeta invalidos\"}";
        HttpClientErrorException exception = HttpClientErrorException.create(
                HttpStatus.UNPROCESSABLE_ENTITY, "Unprocessable Entity", null, errorBody.getBytes(StandardCharsets.UTF_8), null
        );
        when(restTemplate.exchange(anyString(), any(), any(), any(ParameterizedTypeReference.class)))
                .thenThrow(exception);
        when(objectMapper.readTree(errorBody)).thenReturn(new ObjectMapper().readTree(errorBody));
        Exception thrown = assertThrows(InvalidCardDataException.class, () -> {
            registerCardUseCase.registerCard(tokenizeRequest, customer, mockApiKey);
        });
        assertTrue(thrown.getMessage().contains("Datos de tarjeta invalidos"));
        verify(cardRepositoryPort, never()).save(any(TokenizedCard.class));
    }

    @Test
    void whenTokenizationFails_withGenericErrorMessage_shouldThrowTokenizationException() throws JsonProcessingException {
        String errorBody = "{\"error\":\"Tokenización fallida\"}";
        HttpClientErrorException exception = HttpClientErrorException.create(
                HttpStatus.BAD_REQUEST, "Bad Request", null, errorBody.getBytes(StandardCharsets.UTF_8), null
        );
        when(restTemplate.exchange(anyString(), any(), any(), any(ParameterizedTypeReference.class)))
                .thenThrow(exception);
        when(objectMapper.readTree(errorBody)).thenReturn(new ObjectMapper().readTree(errorBody));
        Exception thrown = assertThrows(TokenizationException.class, () -> {
            registerCardUseCase.registerCard(tokenizeRequest, customer, mockApiKey);
        });
        assertTrue(thrown.getMessage().contains("Tokenización fallida"));
        verify(cardRepositoryPort, never()).save(any(TokenizedCard.class));
    }
}