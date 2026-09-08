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
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@KafkaListener(
        topics = KafkaTopics.TRANSACTION_COMMANDS,
        groupId = "transaction-saga-group")
public class TransactionSagaConsumer {

    private final TransactionService transactionService;
    private final TransactionSagaEventProducer transactionSagaEventProducer;

    @KafkaHandler
    public void handleCreateTransaction(CreateTransactionCommand command) {

        log.info("Received CREATE_TRANSACTION command | paymentReference={}",
                command.paymentReference());

        try {
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

        } catch (Exception e) {
            log.error("Transaction creation failed | paymentReference={}",
                    command.paymentReference(), e);

            transactionSagaEventProducer.publishTransactionFailed(
                    new TransactionFailedEvent(
                            command.paymentReference(),
                            e.getMessage()));
        }
    }
}
