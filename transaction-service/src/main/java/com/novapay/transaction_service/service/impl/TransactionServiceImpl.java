package com.novapay.transaction_service.service.impl;

import com.novapay.transaction_service.client.WalletClient;
import com.novapay.transaction_service.dto.request.CreditRequest;
import com.novapay.transaction_service.dto.request.DebitRequest;
import com.novapay.transaction_service.dto.request.TransactionRequest;
import com.novapay.transaction_service.dto.response.TransactionResponse;
import com.novapay.transaction_service.dto.response.TransactionStatusResponse;
import com.novapay.transaction_service.entity.Transaction;
import com.novapay.transaction_service.entity.enums.TransactionStatus;
import com.novapay.transaction_service.entity.enums.TransactionType;
import com.novapay.transaction_service.exception.ResourceNotFoundException;
import com.novapay.transaction_service.mapper.TransactionMapper;
import com.novapay.transaction_service.repository.TransactionRepository;
import com.novapay.transaction_service.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionServiceImpl.class);

    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;
    private final WalletClient walletClient;

    @Transactional
    @Override
    public TransactionResponse transferMoney(TransactionRequest request) {

        LOGGER.info("Transfer request received | senderWalletId={} receiverWalletId={} amount={}",
                request.getSenderWalletId(),
                request.getReceiverWalletId(),
                request.getAmount());

        if (request.getSenderWalletId()
                .equals(request.getReceiverWalletId())) {

            LOGGER.warn("Transfer failed: Sender and Receiver wallet are same | walletId={}",
                    request.getSenderWalletId());
            throw new IllegalArgumentException("Sender and Receiver cannot be same");
        }

        walletClient.debitWallet(
                new DebitRequest(
                        request.getSenderWalletId(),
                        request.getAmount()));

        walletClient.creditWallet(
                new CreditRequest(
                        request.getReceiverWalletId(),
                        request.getAmount()));

        Transaction transaction = Transaction.builder()
                .senderWalletId(request.getSenderWalletId())
                .receiverWalletId(request.getReceiverWalletId())
                .amount(request.getAmount())
                .type(TransactionType.TRANSFER)
                .status(TransactionStatus.SUCCESS)
                .remarks(request.getRemarks())
                .message("Transfer Successful")
                .build();

        Transaction savedTransaction = transactionRepository.save(transaction);

        LOGGER.info("Transaction completed successfully | referenceId={}", savedTransaction.getReferenceId());
        return transactionMapper.toTransactionResponse(savedTransaction);
    }

    @Transactional(readOnly = true)
    @Override
    public TransactionResponse getTransactionByReferenceId(String referenceId) {
        LOGGER.info("Fetching transaction with reference {}", referenceId);
        Transaction transaction = transactionRepository
                .findByReferenceId(referenceId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with reference " + referenceId));

        LOGGER.info("Transaction fetched successfully | referenceId={} status={} amount={}",
                transaction.getReferenceId(),
                transaction.getStatus(),
                transaction.getAmount());
        return transactionMapper.toTransactionResponse(transaction);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<TransactionResponse> getWalletTransactionHistory(Long walletId, Pageable pageable) {

        LOGGER.info("Fetching transaction history for wallet={} page={} size={}", walletId, pageable.getPageNumber(), pageable.getPageSize());

        Page<Transaction> transactionsPage = transactionRepository.findBySenderWalletIdOrReceiverWalletId(walletId, walletId, pageable);

        LOGGER.info("Wallet transaction history fetch| walletId={} totalRecords={}",
                walletId, transactionsPage.getTotalElements());

        return transactionsPage.map(transactionMapper::toTransactionResponse);
    }

    @Transactional(readOnly = true)
    @Override
    public TransactionStatusResponse getTransactionStatusByReferenceId(String referenceId) {
        LOGGER.info("Fetching transaction status by reference id {}", referenceId);
        Transaction transaction = transactionRepository.findByReferenceId(referenceId).orElseThrow(()
                -> new ResourceNotFoundException("Transaction not found with reference id " + referenceId));
        LOGGER.info("Transaction status fetched successfully | referenceId={}", referenceId);
        return new TransactionStatusResponse(transaction.getReferenceId(), transaction.getStatus().name());
    }

    @Transactional
    @Override
    public TransactionResponse reverseTransaction(String referenceId) {

        LOGGER.info("Reversal request received with reference {}", referenceId);

        Transaction successTransaction = transactionRepository.findByReferenceId(referenceId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with reference id " + referenceId));

        if (successTransaction.getStatus() == TransactionStatus.REVERSED) {
            throw new IllegalStateException("Transaction already reversed with referenceId " + referenceId + successTransaction.getStatus());
        }

        if (successTransaction.getStatus() != TransactionStatus.SUCCESS) {
            throw new IllegalStateException("Only success Transaction status  can be reversed not with referenceId");
        }

        BigDecimal amount = successTransaction.getAmount();

        walletClient.debitWallet(
                new DebitRequest(
                        successTransaction.getReceiverWalletId(),
                        amount));

        walletClient.creditWallet(
                new CreditRequest(
                        successTransaction.getSenderWalletId(),
                        amount));

        Transaction reversalTransaction = Transaction.builder()
                .senderWalletId(successTransaction.getReceiverWalletId())
                .receiverWalletId(successTransaction.getSenderWalletId())
                .amount(amount)
                .type(TransactionType.REVERSAL)
                .status(TransactionStatus.SUCCESS)
                .message("Transaction Reversed Successfully")
                .remarks("Reversal of " + referenceId)
                .build();

        Transaction savedReversal = transactionRepository.save(reversalTransaction);

        successTransaction.setStatus(TransactionStatus.REVERSED);
        transactionRepository.save(successTransaction);

        LOGGER.info("Transaction reversed successfully | originalReferenceId={} reversalReferenceId={}", referenceId, savedReversal.getReferenceId());

        return transactionMapper.toTransactionResponse(savedReversal);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransactionResponse> getAllTransactions(Pageable pageable) {

        LOGGER.info(
                "Fetching all transactions | page={} size={}",
                pageable.getPageNumber(),
                pageable.getPageSize());

        Page<Transaction> transactions = transactionRepository.findAll(pageable);
        LOGGER.info(
                "Transaction fetched successfully | totalRecords={}",
                transactions.getTotalElements());

        return transactions.map(transactionMapper::toTransactionResponse);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<TransactionResponse> getTransactionsByStatus(
            TransactionStatus status,
            Pageable pageable) {

        LOGGER.info(
                "Fetching transactions by status={} page={} size={}",
                status,
                pageable.getPageNumber(),
                pageable.getPageSize());

        Page<Transaction> transactions =
                transactionRepository.findByStatus(status, pageable);

        LOGGER.info(
                "Transactions fetched successfully | status={} totalRecords={}",
                status,
                transactions.getTotalElements());

        return transactions.map(transactionMapper::toTransactionResponse);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<TransactionResponse> getTransactionsByDateRange(
            LocalDateTime from,
            LocalDateTime to,
            Pageable pageable) {

        LOGGER.info(
                "Fetching transactions between {} and {}",
                from,
                to);

        Page<Transaction> transactions =
                transactionRepository.findByCreatedAtBetween(
                        from,
                        to,
                        pageable);

        LOGGER.info("Transactions fetched successfully | totalRecords={}", transactions.getTotalElements());

        return transactions.map(transactionMapper::toTransactionResponse);
    }
}
