package com.farmatodo.ecommerce.application.usecase;


import com.farmatodo.ecommerce.application.dto.RegisterCardResponse;
import com.farmatodo.ecommerce.application.dto.TokenizeRequest;
import com.farmatodo.ecommerce.application.dto.TokenizeResponse;

import com.farmatodo.ecommerce.domain.exception.CardAlreadyExistsException;
import com.farmatodo.ecommerce.domain.exception.InvalidCardDataException;
import com.farmatodo.ecommerce.domain.exception.TokenizationException;

import com.farmatodo.ecommerce.domain.model.Customer;
import com.farmatodo.ecommerce.domain.model.TokenizedCard;

import com.farmatodo.ecommerce.domain.port.in.RegisterCardUseCase;
import com.farmatodo.ecommerce.domain.port.out.TokenizedCardRepositoryPort;

import com.farmatodo.ecommerce.infrastructure.common.ApiResponse;
import com.farmatodo.ecommerce.infrastructure.security.EncryptionService;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class RegisterCardUseCaseImpl implements RegisterCardUseCase {

    private final RestTemplate restTemplate;
    private final EncryptionService encryptionService;
    private final TokenizedCardRepositoryPort cardRepositoryPort;
    private final ObjectMapper objectMapper;

    @Value("${security.api-key}")
    private String configuredApiKey;

    @Value("${api.base-url}")
    private String apiBaseUrl;

    @Value("${server.port:8080}")
    private String serverPort;

    @Value("${server.servlet.context-path:/api/v1}")
    private String contextPath;

    private String getTokenizeUrl() {
        return apiBaseUrl + "/tokenize";
    }

    @Override
    @Transactional
    public RegisterCardResponse registerCard(TokenizeRequest request, Customer customer, String apikey) {

        TokenizeResponse tokenizedData;

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-API-KEY", apikey);

            HttpEntity<TokenizeRequest> entity = new HttpEntity<>(request, headers);

            ResponseEntity<ApiResponse<TokenizeResponse>> response = restTemplate.exchange(
                    getTokenizeUrl(),
                    HttpMethod.POST,
                    entity,
                    new ParameterizedTypeReference<ApiResponse<TokenizeResponse>>() {}
            );

            tokenizedData = Objects.requireNonNull(response.getBody()).getData();

        } catch (RestClientResponseException e) {
            String errorBody = e.getResponseBodyAsString();
            String errorMessage = "Unknown tokenization error";

            try {
                if (errorBody != null && !errorBody.isEmpty()) {
                    JsonNode errorNode = objectMapper.readTree(errorBody);
                    errorMessage = errorNode.path("error").asText("Tokenización fallida");
                } else {
                    errorMessage = e.getMessage();
                }
            } catch (IOException parseException) {
                errorMessage = e.getMessage() != null ? e.getMessage() : "HTTP Error " + e.getStatusCode().value();
            }

            if (errorMessage.contains("Tokenization fallida")) {
                throw new TokenizationException(errorMessage);
            } else if (errorMessage.contains("Datos de tarjeta invalidos")) {
                throw new InvalidCardDataException(errorMessage);
            }  else if (errorMessage.contains("APIKEY invalida. Por favor revise el formato.")) {
                System.out.println(errorMessage);
                throw new IllegalArgumentException(errorMessage);
            }else {
                throw new TokenizationException("Internal tokenization error: " + errorMessage);
            }
        }

        if (cardRepositoryPort.existsByCustomerIdAndLastFourDigitsAndCardBrand(
                customer.getId(),
                tokenizedData.getLastFourDigits(),
                tokenizedData.getCardBrand()
        )) {
            throw new CardAlreadyExistsException(
                    "Esta tarjeta (" + tokenizedData.getCardBrand() +
                            " que termina en " + tokenizedData.getLastFourDigits() +
                            ") ya se encuentra registrada."
            );
        }

        String encryptedMonth = encryptionService.encrypt(String.valueOf(request.getExpirationMonth()));
        String encryptedYear = encryptionService.encrypt(String.valueOf(request.getExpirationYear()));

        TokenizedCard newCard = TokenizedCard.builder()
                .customerId(customer.getId())
                .token(tokenizedData.getToken())
                .lastFourDigits(tokenizedData.getLastFourDigits())
                .cardBrand(tokenizedData.getCardBrand())
                .cardholderName(request.getCardHolderName())
                .expirationMonth(encryptedMonth)
                .expirationYear(encryptedYear)
                .isActive(true)
                .isDefault(false)
                .build();

        TokenizedCard savedCard = cardRepositoryPort.save(newCard);

        return RegisterCardResponse.builder()
                .cardId(savedCard.getUuid())
                .lastFourDigits(savedCard.getLastFourDigits())
                .cardBrand(savedCard.getCardBrand())
                .isDefault(savedCard.isDefault())
                .build();
    }
}