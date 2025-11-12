package com.farmatodo.ecommerce.domain.model;

import lombok.Builder;
import lombok.Data;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
public class ProductSearch {
    private Long id;
    private UUID uuid;
    private Long customerId;
    private String searchQuery;
    private int resultsCount;
    private String ipAddress;
    private String userAgent;
    private OffsetDateTime searchedAt;
}