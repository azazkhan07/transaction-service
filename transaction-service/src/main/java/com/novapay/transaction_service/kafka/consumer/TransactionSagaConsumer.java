package com.novapay.transaction_service.kafka.consumer;

import com.novapay.transaction_service.dto.request.TransactionRequest;
import com.novapay.transaction_service.dto.response.TransactionResponse;
import com.novapay.transaction_service.kafka.constants.KafkaTopics;
import com.novapay.transaction_service.kafka.producer.TransactionSagaEventProducer;
import com.novapay.transaction_service.saga.event.CreateTransactionCommand;
import com.novapay.transaction_service.saga.event.TransactionCreatedEvent;
import com.novapay.transaction_service.saga.event.TransactionFailedEvent;
import com.novapay.transaction_service.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@RetryableTopic(
        attempts = "4",
        backoff = @Backoff(
                delay = 2000,
                multiplier = 2.0))
@KafkaListener(
        topics = KafkaTopics.TRANSACTION_COMMANDS,
        groupId = "transaction-saga-group")
public class TransactionSagaConsumer {

    private final TransactionService transactionService;
    private final TransactionSagaEventProducer transactionSagaEventProducer;

    @KafkaHandler
    public void handleCreateTransaction(CreateTransactionCommand command) {

        log.info(
                "Received CREATE_TRANSACTION command | paymentReference={}",
                command.paymentReference());

        TransactionRequest request = new TransactionRequest();

        request.setSenderWalletId(command.payerWalletId());
        request.setReceiverWalletId(command.payeeWalletId());
        request.setAmount(command.amount());
        request.setRemarks(null);

        TransactionResponse response =
                transactionService.createSagaTransaction(request);

        transactionSagaEventProducer.publishTransactionCreated(
                new TransactionCreatedEvent(
                        command.paymentReference(),
                        command.payerWalletId(),
                        command.payeeWalletId(),
                        command.amount(),
                        response.referenceId()));

    }

    @DltHandler
    public void handleDlt(CreateTransactionCommand command) {

        log.error("Message moved to DLT after retries exhausted | paymentReference={}", command.paymentReference());

        transactionSagaEventProducer.publishTransactionFailed(
                new TransactionFailedEvent(
                        command.paymentReference(),
                        "Transaction creation failed after retries"));
    }
}
