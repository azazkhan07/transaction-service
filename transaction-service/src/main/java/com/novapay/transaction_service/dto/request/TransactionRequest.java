package com.novapay.transaction_service.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
public class TransactionRequest {
    @NotNull(message = "sender walletId is required")
    private Long senderWalletId;
    @NotNull(message = "receiver walletId is required")
    private Long receiverWalletId;
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "1.0", message = "Amount must be greater than 0" )
    private BigDecimal amount;
    private String remarks;
}
