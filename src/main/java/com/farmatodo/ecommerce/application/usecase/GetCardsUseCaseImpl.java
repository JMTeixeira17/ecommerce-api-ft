package com.farmatodo.ecommerce.application.usecase;

import com.farmatodo.ecommerce.application.dto.RegisterCardResponse;
import com.farmatodo.ecommerce.application.mapper.CardApiMapper;
import com.farmatodo.ecommerce.domain.model.Customer;
import com.farmatodo.ecommerce.domain.model.TokenizedCard;
import com.farmatodo.ecommerce.domain.port.in.GetCardsUseCase;
import com.farmatodo.ecommerce.domain.port.out.TokenizedCardRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetCardsUseCaseImpl implements GetCardsUseCase {

    private final TokenizedCardRepositoryPort cardRepositoryPort;
    private final CardApiMapper cardApiMapper;

    @Override
    @Transactional(readOnly = true)
    public List<RegisterCardResponse> getCustomerCards(Customer customer) {
        List<TokenizedCard> cards = cardRepositoryPort.findByCustomerIdAndIsActiveTrue(customer.getId());
        return cardApiMapper.toResponseList(cards);
    }
}