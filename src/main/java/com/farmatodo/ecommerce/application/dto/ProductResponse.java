package com.farmatodo.ecommerce.application.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.UUID;


@Data
@Builder
public class ProductResponse {
    private UUID uuid;
    private String sku;
    private String name;
    private String description;
    private BigDecimal price;
    private int stock;
    private String category;
    private String brand;
    private String imageUrl;
}