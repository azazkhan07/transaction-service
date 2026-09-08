package com.novapay.transaction_service.service;


import com.novapay.transaction_service.dto.request.TransactionRequest;
import com.novapay.transaction_service.dto.response.TransactionResponse;
import com.novapay.transaction_service.dto.response.TransactionStatusResponse;
import com.novapay.transaction_service.entity.enums.TransactionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface TransactionService {

    TransactionResponse transferMoney(TransactionRequest request);

    TransactionResponse getTransactionByReferenceId(String referenceId);

    Page<TransactionResponse> getWalletTransactionHistory(Long walletId, Pageable pageable);

    TransactionStatusResponse getTransactionStatusByReferenceId(String referenceId);

    TransactionResponse reverseTransaction(String referenceId);

    Page<TransactionResponse> getAllTransactions(Pageable pageable);

    Page<TransactionResponse> getTransactionsByStatus(TransactionStatus status, Pageable pageable);

    Page<TransactionResponse> getTransactionsByDateRange(LocalDateTime from, LocalDateTime to, Pageable pageable);

    TransactionResponse createSagaTransaction(TransactionRequest request);
}
