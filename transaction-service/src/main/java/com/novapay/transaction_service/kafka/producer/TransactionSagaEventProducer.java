package com.novapay.transaction_service.kafka.producer;

import com.novapay.transaction_service.kafka.constants.KafkaTopics;
import com.novapay.transaction_service.saga.event.TransactionCreatedEvent;
import com.novapay.transaction_service.saga.event.TransactionFailedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransactionSagaEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishTransactionCreated(TransactionCreatedEvent event) {

        log.info("Publishing TRANSACTION_CREATED event | paymentReference={} transactionReference={}",
                event.paymentReference(),
                event.transactionReference());

        kafkaTemplate.send(
                KafkaTopics.TRANSACTION_EVENTS,
                event.paymentReference(),
                event);
    }

    public void publishTransactionFailed(TransactionFailedEvent event) {

        log.info("Publishing TRANSACTION_FAILED event | paymentReference={} reason={}",
                event.paymentReference(),
                event.reason());

        kafkaTemplate.send(KafkaTopics.TRANSACTION_EVENTS,
                event.paymentReference(),
                event);
    }
}
