package uk.gov.companieshouse.paymentreconciliation.consumer.util;

import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.paymentreconciliation.consumer.exception.NonRetryableException;
import uk.gov.companieshouse.paymentreconciliation.consumer.logging.DataMapHolder;

import static org.springframework.kafka.retrytopic.RetryTopicHeaders.DEFAULT_HEADER_ATTEMPTS;
import static uk.gov.companieshouse.paymentreconciliation.consumer.Application.NAMESPACE;

import java.nio.ByteBuffer;
import java.util.Optional;

import org.springframework.messaging.MessageHeaders;

import payments.payment_processed;

public class KafkaUtils {

    private KafkaUtils() {
        // util class
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(NAMESPACE);

    public static payment_processed extractRefundRequest(Object payload) {
        if (payload instanceof payment_processed paymentReconciliation) {
            return paymentReconciliation;
        }
        String errorMessage = "Invalid payload type, payload: [%s]".formatted(payload.toString());
        LOGGER.error(errorMessage, DataMapHolder.getLogMap());
        throw new NonRetryableException(errorMessage);
    }

    public static int getRetryCount(MessageHeaders headers) {
        return Optional.ofNullable(headers.get(DEFAULT_HEADER_ATTEMPTS))
                .map(attempts -> ByteBuffer.wrap((byte[]) attempts).getInt())
                .orElse(1) - 1;
    }
}
