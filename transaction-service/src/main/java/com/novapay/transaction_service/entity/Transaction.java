package com.novapay.transaction_service.entity;


import com.novapay.transaction_service.entity.enums.TransactionStatus;
import com.novapay.transaction_service.entity.enums.TransactionType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "transactions")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false)
    private String referenceId;
    private Long senderWalletId;
    private Long receiverWalletId;
    private BigDecimal amount;
    @Enumerated(EnumType.STRING)
    private TransactionType type;
    @Enumerated(EnumType.STRING)
    private TransactionStatus status;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    private String remarks;
    private String message;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.referenceId = UUID.randomUUID().toString();
    }
}
