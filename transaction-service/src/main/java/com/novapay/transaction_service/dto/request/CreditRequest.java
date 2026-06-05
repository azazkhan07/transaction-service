package com.novapay.transaction_service.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
@AllArgsConstructor
@Setter
@Getter
public class CreditRequest {

    @NotNull(message = "walletId is required")
    private Long walletId;
    @DecimalMin(value = "1.0", message = "Amount must be greater than 0")
    private BigDecimal amount;
}
