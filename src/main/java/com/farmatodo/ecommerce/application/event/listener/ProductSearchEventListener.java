package com.farmatodo.ecommerce.application.event.listener;

import com.farmatodo.ecommerce.application.event.ProductSearchEvent;
import com.farmatodo.ecommerce.domain.model.ProductSearch;
import com.farmatodo.ecommerce.domain.port.out.ProductSearchRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductSearchEventListener {

    private final ProductSearchRepositoryPort productSearchRepository;

    @Async
    @EventListener
    public void handleProductSearchEvent(ProductSearchEvent event) {
        log.info("Processing async product search event for query: {}", event.getSearchQuery());
        try {
            ProductSearch logEntry = ProductSearch.builder()
                    .customerId(event.getCustomerId())
                    .searchQuery(event.getSearchQuery())
                    .resultsCount(event.getResultsCount())
                    .ipAddress(event.getIpAddress())
                    .userAgent(event.getUserAgent())
                    .searchedAt(OffsetDateTime.now())
                    .build();
            productSearchRepository.save(logEntry);
            log.info("Successfully saved async product search log for query: {}", event.getSearchQuery());
        } catch (Exception e) {
            log.error("Failed to save async product search log for query: {}", event.getSearchQuery(), e);
        }
    }
}