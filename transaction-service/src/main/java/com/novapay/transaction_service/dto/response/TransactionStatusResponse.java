package com.novapay.transaction_service.dto.response;

public record TransactionStatusResponse(
        String referenceId,
        String status
) {}