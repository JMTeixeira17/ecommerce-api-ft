package com.farmatodo.ecommerce.infrastructure.adapter.in.rest;

import com.farmatodo.ecommerce.application.dto.RegisterCardResponse;
import com.farmatodo.ecommerce.application.dto.TokenizeRequest;
import com.farmatodo.ecommerce.domain.exception.CardAlreadyExistsException;
import com.farmatodo.ecommerce.domain.exception.TokenizationException;
import com.farmatodo.ecommerce.domain.model.Customer;
import com.farmatodo.ecommerce.domain.port.in.GetCardsUseCase;
import com.farmatodo.ecommerce.domain.port.in.RegisterCardUseCase;
import com.farmatodo.ecommerce.domain.port.out.CustomerRepositoryPort;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = CardController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {
                        com.farmatodo.ecommerce.infrastructure.security.JwtAuthFilter.class,
                        com.farmatodo.ecommerce.infrastructure.security.ApiKeyAuthFilter.class
                }
        )
)
class CardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired private GetCardsUseCase getCardsUseCase;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RegisterCardUseCase registerCardUseCase;

    @MockitoBean
    private GetCardsUseCase getCardUseCase;

    @MockitoBean
    private CustomerRepositoryPort customerRepositoryPort;

    private static final String MOCK_API_KEY = "mock-api-key-for-test";


    private TokenizeRequest createValidCardRequest() {
        TokenizeRequest request = new TokenizeRequest();
        request.setCardNumber("4242424242424242");
        request.setCvv("123");
        request.setExpirationMonth("12");
        request.setExpirationYear("2029");
        request.setCardHolderName("Test User");
        return request;
    }

    @Test
    @WithMockUser(username = "test@user.com", roles = "USER")
    void whenRegisterCard_withValidJwtAndData_shouldSucceed() throws Exception {
        String userEmail = "test@user.com";
        TokenizeRequest request = createValidCardRequest();
        Customer mockCustomer = Customer.builder()
                .id(1L)
                .email(userEmail)
                .build();
        RegisterCardResponse mockResponse = RegisterCardResponse.builder()
                .cardId(UUID.randomUUID())
                .cardBrand("VISA")
                .lastFourDigits("4242")
                .isDefault(false)
                .build();
        when(customerRepositoryPort.findByEmail(userEmail))
                .thenReturn(Optional.of(mockCustomer));
        when(registerCardUseCase.registerCard(any(TokenizeRequest.class), any(Customer.class), anyString()))
                .thenReturn(mockResponse);
        mockMvc.perform(post("/cards")
                        .with(csrf())
                        .header("X-API-KEY", MOCK_API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.cardBrand").value("VISA"))
                .andExpect(jsonPath("$.data.lastFourDigits").value("4242"));
    }

    @Test
    @WithMockUser(username = "test@user.com", roles = "USER")
    void whenRegisterCard_withExistingCard_shouldReturnConflict() throws Exception {
        String userEmail = "test@user.com";
        TokenizeRequest request = createValidCardRequest();
        Customer mockCustomer = Customer.builder().id(1L).email(userEmail).isActive(true).build();
        when(customerRepositoryPort.findByEmail(userEmail)).thenReturn(Optional.of(mockCustomer));
        when(registerCardUseCase.registerCard(any(TokenizeRequest.class), any(Customer.class), anyString()))
                .thenThrow(new CardAlreadyExistsException("Tarjeta ya registrada."));
        mockMvc.perform(post("/cards")
                        .with(csrf())
                        .header("X-API-KEY", MOCK_API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.code").value(409))
                .andExpect(jsonPath("$.error").value("Tarjeta ya registrada."));
    }

    @Test
    @WithMockUser(username = "test@user.com", roles = "USER")
    void whenRegisterCard_isRejected_shouldReturnUnprocessableEntity() throws Exception {
        String userEmail = "test@user.com";
        TokenizeRequest request = createValidCardRequest();
        Customer mockCustomer = Customer.builder().id(1L).email(userEmail).isActive(true).build();
        when(customerRepositoryPort.findByEmail(userEmail)).thenReturn(Optional.of(mockCustomer));
        when(registerCardUseCase.registerCard(any(TokenizeRequest.class), any(Customer.class), anyString()))
                .thenThrow(new TokenizationException("Tokenización rechazada por el proveedor."));
        mockMvc.perform(post("/cards")
                        .with(csrf())
                        .header("X-API-KEY", MOCK_API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.code").value(422))
                .andExpect(jsonPath("$.error").value("Tokenización rechazada por el proveedor."));
    }


    @Test
    @WithMockUser(username = "test@user.com", roles = "USER")
    void whenRegisterCard_withoutApiKeyHeader_shouldReturnBadRequest() throws Exception {
        String userEmail = "test@user.com";
        TokenizeRequest request = createValidCardRequest();
        Customer mockCustomer = Customer.builder().id(1L).email(userEmail).isActive(true).build();
        when(customerRepositoryPort.findByEmail(userEmail)).thenReturn(Optional.of(mockCustomer));
        mockMvc.perform(post("/cards")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.error").value("Se requiere un APIKEY. Por favor, revisa el formato."));
    }

    private RegisterCardResponse createMockCardResponse() {
        return RegisterCardResponse.builder()
                .cardId(UUID.randomUUID())
                .cardBrand("VISA")
                .lastFourDigits("4242")
                .isDefault(false)
                .build();
    }

    @Test
    @WithMockUser(username = "test@user.com")
    void whenGetCards_shouldReturnListOfCards() throws Exception {
        // Arrange
        Customer mockCustomer = Customer.builder().id(1L).email("test@user.com").isActive(true).build();
        RegisterCardResponse card1 = createMockCardResponse();
        RegisterCardResponse card2 = createMockCardResponse();
        List<RegisterCardResponse> cardList = List.of(card1, card2);

        when(customerRepositoryPort.findByEmail("test@user.com")).thenReturn(Optional.of(mockCustomer));
        when(getCardsUseCase.getCustomerCards(any(Customer.class))).thenReturn(cardList);

        // Act & Assert
        mockMvc.perform(get("/cards")
                        .header("Authorization", "Bearer MOCK_VALID_TOKEN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2));
    }
}