package com.novapay.transaction_service.controller;

import com.novapay.transaction_service.dto.request.TransactionRequest;
import com.novapay.transaction_service.dto.response.TransactionResponse;
import com.novapay.transaction_service.dto.response.TransactionStatusResponse;
import com.novapay.transaction_service.entity.enums.TransactionStatus;
import com.novapay.transaction_service.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Tag(name = "Transaction APIs", description = "Money transfer endpoints")
@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionController.class);
    private final TransactionService transactionService;

    @Operation(summary = "Transfer money between wallets")
    @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Transfer successful"),
    @ApiResponse(responseCode = "400", description = "Invalid request / insufficient balance"),
    @ApiResponse(responseCode = "404", description = "Wallet not found"),
    @ApiResponse(responseCode = "500", description = "Internal server error")})
    @PostMapping("/transfer")
    public ResponseEntity<TransactionResponse> transferMoney(@Valid @RequestBody TransactionRequest transactionRequest) {

        LOGGER.info("Incoming transfer request | sender={} receiver={} amount={}",
                transactionRequest.getSenderWalletId(),
                transactionRequest.getReceiverWalletId(),
                transactionRequest.getAmount());
        TransactionResponse transactionResponse = transactionService.transferMoney(transactionRequest);

        LOGGER.info("Transfer completed successfully referenceId: {}", transactionResponse.referenceId());
        return ResponseEntity.status(HttpStatus.OK).body(transactionResponse);
    }

    @Operation(summary = "Get transaction by reference id")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Transaction found"),
    @ApiResponse(responseCode = "404", description = "Transaction not found")})
    @GetMapping("/{referenceId}")
    public ResponseEntity<TransactionResponse> getTransaction(@PathVariable String referenceId) {
        TransactionResponse response = transactionService.getTransactionByReferenceId(referenceId);
        LOGGER.info("Transaction found with referenceId: {}", referenceId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(summary = "Get wallet transaction history")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Transaction history fetched"),
    @ApiResponse(responseCode = "404", description = "Wallet not found")})
    @GetMapping("/wallet/{walletId}")
    public ResponseEntity<Page<TransactionResponse>> getWalletHistory(
            @PathVariable Long walletId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.fromString(sortDirection), sortBy));

        LOGGER.info("History request | walletId={}, page={}, size={} sortDirection={}", walletId, page, size, sortDirection);
        return ResponseEntity.status(HttpStatus.OK).body(transactionService.getWalletTransactionHistory(walletId, pageable));
    }

    @Operation(summary = "Get transaction status")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Transaction status fetched"),
    @ApiResponse(responseCode = "404", description = "Transaction status not found")})
    @GetMapping("/status/{referenceId}")
    public ResponseEntity<TransactionStatusResponse> getTransactionStatus(@PathVariable String referenceId) {
        LOGGER.info("Fetching transaction status for referenceId: {}", referenceId);
        return ResponseEntity.status(HttpStatus.OK).body(transactionService.getTransactionStatusByReferenceId(referenceId));
    }

    @Operation(summary = "Reverse a successful transaction")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Refund successfully"),
    @ApiResponse(responseCode = "404", description = "transaction not found"),
    @ApiResponse(responseCode = "400", description = "Transaction can not be reversed")})
    @PutMapping("/reverse/{referenceId}")
    public ResponseEntity<TransactionResponse> reverseTransaction(
            @PathVariable String referenceId) {

        LOGGER.info("Reversal API called for referenceId: {}", referenceId);

        TransactionResponse response =
                transactionService.reverseTransaction(referenceId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(summary = "Get all transactions")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Transactions fetched successfully"),
    @ApiResponse(responseCode = "404", description = "transactions not found")})
    @GetMapping
    public ResponseEntity<Page<TransactionResponse>> getAllTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.fromString(sortDirection), sortBy));

        LOGGER.info(
                "Fetching all transactions | page={} size={} sortBy={} sortDirection={}",
                page,
                size,
                sortBy,
                sortDirection);

        Page<TransactionResponse> response =
                transactionService.getAllTransactions(pageable);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(summary = "Get transactions by status")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Transactions fetched successfully"),
    @ApiResponse(responseCode = "404", description = "Transaction status not found")})
    @GetMapping("/filter/status/{status}")
    public ResponseEntity<Page<TransactionResponse>> getTransactionsByStatus(
            @PathVariable TransactionStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.fromString(sortDirection), sortBy));

        LOGGER.info(
                "Fetching transactions by status={} page={} size={}",
                status,
                page,
                size);

        return ResponseEntity.ok(
                transactionService.getTransactionsByStatus(
                        status,
                        pageable));
    }

    @Operation(summary = "Get transactions by date range")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Transactions fetched successfully"),
    @ApiResponse(responseCode = "404", description = "Transaction status not found")})
    @GetMapping("/filter/date-range")
    public ResponseEntity<Page<TransactionResponse>> getTransactionsByDateRange(
            @RequestParam LocalDate from,
            @RequestParam LocalDate to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.fromString(sortDirection),
                        sortBy));

        LocalDateTime fromDateTime = from.atStartOfDay();

        LocalDateTime toDateTime =
                to.atTime(23, 59, 59);

        LOGGER.info(
                "Fetching transactions from={} to={}",
                from,
                to);

        return ResponseEntity.ok(transactionService.getTransactionsByDateRange(
                        fromDateTime,
                        toDateTime,
                        pageable));
    }
}
