package com.farmatodo.ecommerce.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GeneratedColumn;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "tokenized_cards")
@Data
public class TokenizedCardEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @GeneratedColumn("DEFAULT")
    @Column(name = "uuid", unique = true, nullable = false, updatable = false, columnDefinition = "UUID DEFAULT uuid_generate_v4()")
    private UUID uuid;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "token", unique = true, nullable = false)
    private String token;

    @Column(name = "last_four_digits", nullable = false, length = 4)
    private String lastFourDigits;

    @Column(name = "card_brand", nullable = false, length = 20)
    private String cardBrand;

    @Column(name = "cardholder_name", nullable = false)
    private String cardholderName;

    @Column(name = "expiration_month", nullable = false)
    private String expirationMonth;

    @Column(name = "expiration_year", nullable = false)
    private String expirationYear;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @Column(name = "is_default", nullable = false)
    private boolean isDefault;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "last_used_at")
    private OffsetDateTime lastUsedAt;
}