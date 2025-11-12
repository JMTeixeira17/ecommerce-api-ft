package com.farmatodo.ecommerce.application.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;


@Getter
public class ProductSearchEvent extends ApplicationEvent {

    private final Long customerId;
    private final String searchQuery;
    private final int resultsCount;
    private final String ipAddress;
    private final String userAgent;

    public ProductSearchEvent(Object source, Long customerId, String searchQuery, int resultsCount, String ipAddress, String userAgent) {
        super(source);
        this.customerId = customerId;
        this.searchQuery = searchQuery;
        this.resultsCount = resultsCount;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
    }
}