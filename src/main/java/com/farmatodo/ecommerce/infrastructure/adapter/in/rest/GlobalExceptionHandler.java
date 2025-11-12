package com.farmatodo.ecommerce.infrastructure.adapter.in.rest;

import com.farmatodo.ecommerce.domain.exception.*;
import com.farmatodo.ecommerce.infrastructure.common.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomerAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Object>> handleCustomerAlreadyExists(CustomerAlreadyExistsException ex) {
        ApiResponse<Object> response = ApiResponse.error(ex.getMessage(), HttpStatus.CONFLICT.value());
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(CardAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Object>> handleCardAlreadyExists(CardAlreadyExistsException ex) {
        ApiResponse<Object> response = ApiResponse.error(ex.getMessage(), HttpStatus.CONFLICT.value());
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .collect(Collectors.toList());

        String errorMessage = String.join(", ", errors);
        ApiResponse<Object> response = ApiResponse.error(errorMessage, HttpStatus.BAD_REQUEST.value());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Object>> handleBadCredentials(BadCredentialsException ex) {
        ApiResponse<Object> response = ApiResponse.error("Usuario o contraseña inválido.", HttpStatus.UNAUTHORIZED.value());
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(TokenizationException.class)
    public ResponseEntity<ApiResponse<Object>> handleTokenizationRejection(TokenizationException ex) {
        ApiResponse<Object> response = ApiResponse.error(ex.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY.value());
        return new ResponseEntity<>(response, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler(InvalidCardDataException.class)
    public ResponseEntity<ApiResponse<Object>> handleInvalidCardData(InvalidCardDataException ex) {
        ApiResponse<Object> response = ApiResponse.error(ex.getMessage(), HttpStatus.BAD_REQUEST.value());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGenericException(Exception ex) {
        ex.printStackTrace();
        ApiResponse<Object> response = ApiResponse.error("An internal server error occurred.", HttpStatus.INTERNAL_SERVER_ERROR.value());
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(SearchQueryException.class)
    public ResponseEntity<ApiResponse<Object>> handleSearchQueryException(SearchQueryException ex) {
        ApiResponse<Object> response = ApiResponse.error(ex.getMessage(), HttpStatus.BAD_REQUEST.value());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ApiResponse<Object>> handleInsufficientStock(InsufficientStockException ex) {
        ApiResponse<Object> response = ApiResponse.error(ex.getMessage(), HttpStatus.CONFLICT.value());
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleProductNotFound(ProductNotFoundException ex) {
        ApiResponse<Object> response = ApiResponse.error(ex.getMessage(), HttpStatus.NOT_FOUND.value());
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(CartNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleCartNotFound(CartNotFoundException ex) {
        ApiResponse<Object> response = ApiResponse.error(ex.getMessage(), HttpStatus.NOT_FOUND.value());
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }


    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleOrderNotFound(OrderNotFoundException ex) {
        ApiResponse<Object> response = ApiResponse.error(ex.getMessage(), HttpStatus.NOT_FOUND.value());
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(PaymentException.class)
    public ResponseEntity<ApiResponse<Object>> handlePaymentException(PaymentException ex) {
        ApiResponse<Object> response = ApiResponse.error(ex.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY.value());
        return new ResponseEntity<>(response, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Object>> handleIllegalArgument(IllegalArgumentException ex) {
        String message = "Argumento de petición inválido. Por favor, revisa el formato.";
        System.out.println(ex.getMessage());
        HttpStatus status = HttpStatus.BAD_REQUEST;
        if (ex.getMessage() != null) {
            if (ex.getMessage().contains("Se requiere un APIKEY.")) {
                message = "Se requiere un APIKEY. Por favor, revisa el formato.";
                status = HttpStatus.UNAUTHORIZED;
            } else if (ex.getMessage().contains("Invalid UUID string")) {
                message = "El UUID proporcionado tiene un formato inválido.";
            } else if(ex.getMessage().contains("APIKEY invalida. Por favor revise el formato.")){
                message = "APIKEY invalida. Por favor, revise el formato.";
                status = HttpStatus.UNAUTHORIZED;
            }
        }
        ApiResponse<Object> response = ApiResponse.error(message, status.value());
        return new ResponseEntity<>(response, status);
    }

    @ExceptionHandler(InvalidConfigValueException.class)
    public ResponseEntity<ApiResponse<Object>> InvalidConfigValueException(InvalidConfigValueException ex) {
        ApiResponse<Object> response = ApiResponse.error(ex.getMessage(), HttpStatus.CONFLICT.value());
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

}