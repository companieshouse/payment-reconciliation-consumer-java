package uk.gov.companieshouse.paymentreconciliation.consumer.kafka;

import static org.springframework.kafka.support.KafkaHeaders.EXCEPTION_MESSAGE;
import static org.springframework.kafka.support.KafkaHeaders.ORIGINAL_OFFSET;
import static org.springframework.kafka.support.KafkaHeaders.ORIGINAL_PARTITION;
import static org.springframework.kafka.support.KafkaHeaders.ORIGINAL_TOPIC;
import static uk.gov.companieshouse.paymentreconciliation.consumer.Application.NAMESPACE;

import java.math.BigInteger;
import java.util.Map;
import java.util.Optional;
import org.apache.kafka.clients.producer.ProducerInterceptor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.header.Headers;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.paymentreconciliation.consumer.logging.DataMapHolder;

public class InvalidMessageRouter implements ProducerInterceptor<String, Object> {

    private static final Logger LOGGER = LoggerFactory.getLogger(NAMESPACE);

    private MessageFlags messageFlags;
    private String invalidTopic;

    @Override
    public ProducerRecord<String, Object> onSend(ProducerRecord<String, Object> producerRecord) {
        if (messageFlags.isRetryable()) {
            messageFlags.destroy();
            return producerRecord;
        } else {

            Headers headers = producerRecord.headers();
            String originalTopic = Optional.ofNullable(headers.lastHeader(ORIGINAL_TOPIC))
                    .map(h -> new String(h.value())).orElse(producerRecord.topic());
            BigInteger partition = Optional.ofNullable(headers.lastHeader(ORIGINAL_PARTITION))
                    .map(h -> new BigInteger(h.value())).orElse(BigInteger.valueOf(-1));
            BigInteger offset = Optional.ofNullable(headers.lastHeader(ORIGINAL_OFFSET))
                    .map(h -> new BigInteger(h.value())).orElse(BigInteger.valueOf(-1));
            String exception = Optional.ofNullable(headers.lastHeader(EXCEPTION_MESSAGE))
                    .map(h -> new String(h.value())).orElse("unknown");

            LOGGER.error("""
                            Republishing record to topic: [%s] \
                            From: original topic: [%s], partition: [%s], offset: [%s], exception: [%s]\
                            """.formatted(invalidTopic, originalTopic, partition, offset, exception),
                    DataMapHolder.getLogMap());

            return new ProducerRecord<>(invalidTopic, producerRecord.key(), producerRecord.value());
        }
    }

    @Override
    public void onAcknowledgement(RecordMetadata metadata, Exception exception) {
        // intentionally left blank

    }

    @Override
    public void close() {
        // intentionally left blank
    }

    @Override
    public void configure(Map<String, ?> configs) {
        this.messageFlags = (MessageFlags) configs.get("message-flags");
        this.invalidTopic = (String) configs.get("invalid-topic");
    }
}
