package com.novapay.transaction_service.saga.event;

public record TransactionFailedEvent(
        String paymentReference,
        String reason
) { }
