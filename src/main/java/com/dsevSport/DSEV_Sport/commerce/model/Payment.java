package com.dsevSport.DSEV_Sport.commerce.model;

import com.dsevSport.DSEV_Sport.common.util.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter @Setter @Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "payments")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @OneToOne
    @JoinColumn(name = "order_id", insertable = false, updatable = false, nullable = false)
    Order order;

    @Column(name = "amount", nullable = false)
    BigDecimal amount;

    @Column(name = "payment_method", nullable = false)
    String paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status")
    PaymentStatus status;

    @Column(name = "transaction_id", unique = true, nullable = false)
    String transactionId;

    @Column(name = "bank_code")
    String bankCode;

    @Column(name = "created_at")
    LocalDateTime createdAt;
}
