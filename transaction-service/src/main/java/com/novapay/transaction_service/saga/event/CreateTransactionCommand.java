package com.novapay.transaction_service.saga.event;

import java.math.BigDecimal;

public record CreateTransactionCommand(
        String paymentReference,
        Long payerWalletId,
        Long payeeWalletId,
        BigDecimal amount
) { }
