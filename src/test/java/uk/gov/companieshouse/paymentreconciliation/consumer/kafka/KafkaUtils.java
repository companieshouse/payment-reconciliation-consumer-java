package uk.gov.companieshouse.paymentreconciliation.consumer.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;

import java.time.Duration;

public final class KafkaUtils {

    static final String MAIN_TOPIC = "cidev-payment-processed";
    static final String RETRY_TOPIC = "cidev-payment-processed-payment-reconciliation-consumer-retry";
    static final String ERROR_TOPIC = "cidev-payment-processed-payment-reconciliation-consumer-error";
    static final String INVALID_TOPIC = "cidev-payment-processed-payment-reconciliation-consumer-invalid";

    private KafkaUtils() {
    }

    static int noOfRecordsForTopic(ConsumerRecords<?, ?> records, String topic) {
        int count = 0;
        for (@SuppressWarnings("unused") ConsumerRecord<?, ?> ignored : records.records(topic)) {
            count++;
        }
        return count;
    }

    static Duration kafkaPollingDuration() {
        String kafkaPollingDuration = System.getenv().containsKey("KAFKA_POLLING_DURATION") ?
                System.getenv("KAFKA_POLLING_DURATION") : "1000";
        return Duration.ofMillis(Long.parseLong(kafkaPollingDuration));
    }
}
