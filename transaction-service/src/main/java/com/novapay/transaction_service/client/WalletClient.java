package com.novapay.transaction_service.client;

import com.novapay.transaction_service.dto.request.CreditRequest;
import com.novapay.transaction_service.dto.request.DebitRequest;
import com.novapay.transaction_service.dto.response.BalanceResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "wallet-service")
public interface WalletClient {

    @GetMapping("/api/v1/wallets/{walletId}/balance")
    BalanceResponse getBalance(@PathVariable Long walletId);

    @PostMapping("/api/v1/wallets/debit")
    void debitWallet(@RequestBody DebitRequest request);

    @PostMapping("/api/v1/wallets/credit")
    void creditWallet(@RequestBody CreditRequest request);
}
