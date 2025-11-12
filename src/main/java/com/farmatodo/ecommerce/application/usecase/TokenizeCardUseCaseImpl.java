package com.farmatodo.ecommerce.application.usecase;

import com.farmatodo.ecommerce.application.dto.TokenizeRequest;
import com.farmatodo.ecommerce.application.dto.TokenizeResponse;
import com.farmatodo.ecommerce.domain.exception.InvalidCardDataException;
import com.farmatodo.ecommerce.domain.exception.TokenizationException;
import com.farmatodo.ecommerce.domain.port.in.TokenizeCardUseCase;
import com.farmatodo.ecommerce.domain.port.out.SystemConfigRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.UUID;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenizeCardUseCaseImpl implements TokenizeCardUseCase {

    private final SystemConfigRepositoryPort systemConfigRepository;

    private static final String REJECTION_KEY = "tokenization.rejection.probability";
    private static final double DEFAULT_REJECTION_PROB = 0.1;

    private static final Pattern VISA_PATTERN = Pattern.compile("^4[0-9]{12}(?:[0-9]{3})?$");
    private static final Pattern MASTERCARD_PATTERN = Pattern.compile("^5[1-5][0-9]{14}$");
    private static final Pattern AMEX_PATTERN = Pattern.compile("^3[47][0-9]{13}$");
    private static final Pattern DISCOVER_PATTERN = Pattern.compile("^6(?:011|5[0-9]{2})[0-9]{12}$");

    @Override
    public TokenizeResponse tokenize(TokenizeRequest request) {
        validateCard(request);
        double rejectionProbability = systemConfigRepository.getValueAsDouble(
                REJECTION_KEY,
                DEFAULT_REJECTION_PROB
        );

        if (Math.random() < rejectionProbability) {
            log.warn("Tokenizacion rechazada ({}%)", rejectionProbability * 100);
            throw new TokenizationException("Tokenizacion rechazada por el proveedor");
        }
        String cardNumber = request.getCardNumber().replaceAll("\\s+", "");
        String brand = detectCardBrand(cardNumber);
        String lastFour = cardNumber.substring(cardNumber.length() - 4);
        String token = "tkn_" + UUID.randomUUID().toString().replace("-", "");
        return TokenizeResponse.builder()
                .token(token)
                .cardBrand(brand)
                .lastFourDigits(lastFour)
                .build();
    }

    private void validateCard(TokenizeRequest request) {
        if (!isValidLuhn(request.getCardNumber())) {
            throw new InvalidCardDataException("Numero de tarjeta inválido (Luhn check failed)");
        }
        try {
            int month = Integer.parseInt(request.getExpirationMonth());
            int year = Integer.parseInt(request.getExpirationYear());

            if (month < 1 || month > 12) {
                throw new InvalidCardDataException("Mes de expiración inválido");
            }

            LocalDate today = LocalDate.now();
            LocalDate expiryDate = LocalDate.of(year, month, 1).plusMonths(1).minusDays(1);

            if (expiryDate.isBefore(today)) {
                throw new InvalidCardDataException("Tarjeta está expirada");
            }

        } catch (NumberFormatException e) {
            throw new InvalidCardDataException("El mes o año de expiración no son un número");
        }

        if (request.getCvv() == null || !request.getCvv().matches("^[0-9]{3,4}$")) {
            throw new InvalidCardDataException("CVV inválido");
        }
    }

    private String detectCardBrand(String cardNumber) {
        if (VISA_PATTERN.matcher(cardNumber).matches()) return "VISA";
        if (MASTERCARD_PATTERN.matcher(cardNumber).matches()) return "MASTERCARD";
        if (AMEX_PATTERN.matcher(cardNumber).matches()) return "AMEX";
        if (DISCOVER_PATTERN.matcher(cardNumber).matches()) return "DISCOVER";
        throw new InvalidCardDataException("Marca de tarjeta no soportada.");
    }

    private boolean isValidLuhn(String cardNumber) {
        String cleanedNumber = cardNumber.replaceAll("\\s+", "");
        int nDigits = cleanedNumber.length();
        int nSum = 0;
        boolean isSecond = false;
        for (int i = nDigits - 1; i >= 0; i--) {
            int d = cleanedNumber.charAt(i) - '0';
            if (isSecond) {
                d = d * 2;
            }
            nSum += d / 10;
            nSum += d % 10;
            isSecond = !isSecond;
        }
        return (nSum % 10 == 0);
    }
}