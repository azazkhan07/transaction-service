package com.novapay.transaction_service.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

public record BalanceResponse(
        @Schema(example = "WLHGUGUSXJ")
        Long walletId,
        @Schema(example = "1.00")
        BigDecimal availableBalance,
        @Schema(example = "1.00")
        BigDecimal blockedBalance) {}
