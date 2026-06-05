package com.novapay.transaction_service.dto.response;


import com.novapay.transaction_service.entity.enums.TransactionStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record TransactionResponse(
        @Schema(example = "ref_id:4tbsertbbg")
        String referenceId,
        @Schema(example = "1")
        BigDecimal amount,
        @Schema(example = "PENDING")
        TransactionStatus status,
        @Schema(example = "2026-02-06T12:30:00")
        LocalDateTime createdAt,
        @Schema(example = "Transaction Successful")
        String remarks,
        @Schema(example = "Payment transfer successful")
        String message

) {}

