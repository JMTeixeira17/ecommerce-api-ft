package com.farmatodo.ecommerce.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.GeneratedColumn;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "product_searches")
@Data
public class ProductSearchEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @GeneratedColumn("DEFAULT")
    @Column(name = "uuid", unique = true, nullable = false, updatable = false,
            columnDefinition = "UUID DEFAULT uuid_generate_v4()")
    private UUID uuid;

    @Column(name = "customer_id")
    private Long customerId;

    @Column(name = "search_query", nullable = false, length = 500)
    private String searchQuery;

    @Column(name = "results_count", nullable = false)
    private int resultsCount;

    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @Column(name = "searched_at", nullable = false, updatable = false,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private OffsetDateTime searchedAt;
}