package com.novapay.transaction_service.saga.event;

import java.math.BigDecimal;

public record TransactionCreatedEvent(
        String paymentReference,
        Long payerWalletId,
        Long payeeWalletId,
        BigDecimal amount,
        String transactionReference
) { }