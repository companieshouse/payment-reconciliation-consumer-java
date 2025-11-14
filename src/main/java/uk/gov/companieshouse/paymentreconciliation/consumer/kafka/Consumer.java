package uk.gov.companieshouse.paymentreconciliation.consumer.kafka;

import static org.springframework.kafka.support.KafkaHeaders.OFFSET;
import static org.springframework.kafka.support.KafkaHeaders.RECEIVED_PARTITION;
import static org.springframework.kafka.support.KafkaHeaders.RECEIVED_TOPIC;
import static uk.gov.companieshouse.paymentreconciliation.consumer.Application.NAMESPACE;

import java.util.Optional;
import java.util.UUID;

import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Component;

import payments.payment_processed;
import uk.gov.companieshouse.paymentreconciliation.consumer.exception.RetryableException;
import uk.gov.companieshouse.paymentreconciliation.consumer.logging.DataMapHolder;
import uk.gov.companieshouse.paymentreconciliation.consumer.service.PaymentReconciliationServiceRouter;
import uk.gov.companieshouse.paymentreconciliation.consumer.util.KafkaUtils;


@Component
public class Consumer {

    private final PaymentReconciliationServiceRouter router;
    private final MessageFlags messageFlags;
    private static final Logger LOGGER = LoggerFactory.getLogger(NAMESPACE);
    private final int maxAttempts;

    public Consumer(PaymentReconciliationServiceRouter router, MessageFlags messageFlags, @Value("${consumer.max-attempts}") int maxAttempts) {
        this.maxAttempts = maxAttempts;
        this.router = router;
        this.messageFlags = messageFlags;
    }

    @KafkaListener(
            id = "${consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory",
            topics = {"${consumer.topic}"},
            groupId = "${consumer.group-id}")
    public void consume(Message<payment_processed> message) {
        try {
            router.route(message.getPayload());
        } catch (RetryableException ex) {
            messageFlags.setRetryable(true);
            logIfMaxAttemptsReached(message, ex);
            throw ex;
        }
        finally {
         DataMapHolder.clear();
        }
    }

    private void logIfMaxAttemptsReached(Message<payment_processed> message, RetryableException ex) {
        MessageHeaders headers = message.getHeaders();

        int retryCount = KafkaUtils.getRetryCount(headers);

        payment_processed paymentReconciliation = KafkaUtils.extractRefundRequest(message.getPayload());

        DataMapHolder.initialise(Optional.ofNullable(paymentReconciliation.getPaymentResourceId())
                .orElse(UUID.randomUUID().toString()));

        DataMapHolder.get()
                .retryCount(retryCount)
                .topic((String) headers.get(RECEIVED_TOPIC))
                .partition((Integer) headers.get(RECEIVED_PARTITION))
                .offset((Long) headers.get(OFFSET));

        if (retryCount >= maxAttempts - 1) {
            LOGGER.error("Max retry attempts reached", ex, DataMapHolder.getLogMap());
        }
    }
}
