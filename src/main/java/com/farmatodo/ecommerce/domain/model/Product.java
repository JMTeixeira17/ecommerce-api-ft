package com.farmatodo.ecommerce.domain.model;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
public class Product {
    private Long id;
    private UUID uuid;
    private String sku;
    private String name;
    private String description;
    private BigDecimal price;
    private int stock;
    private String category;
    private String brand;
    private String imageUrl;
    private Boolean isActive;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}