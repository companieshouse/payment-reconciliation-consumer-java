package uk.gov.companieshouse.paymentreconciliation.consumer.kafka;

import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.Encoder;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.reflect.ReflectDatumWriter;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import payments.payment_processed;
import uk.gov.companieshouse.paymentreconciliation.consumer.exception.RetryableException;
import uk.gov.companieshouse.paymentreconciliation.consumer.service.PaymentReconciliationServiceRouter;
import uk.gov.companieshouse.paymentreconciliation.consumer.utils.TestUtils;

import java.io.ByteArrayOutputStream;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.companieshouse.paymentreconciliation.consumer.kafka.KafkaUtils.ERROR_TOPIC;
import static uk.gov.companieshouse.paymentreconciliation.consumer.kafka.KafkaUtils.INVALID_TOPIC;
import static uk.gov.companieshouse.paymentreconciliation.consumer.kafka.KafkaUtils.MAIN_TOPIC;
import static uk.gov.companieshouse.paymentreconciliation.consumer.kafka.KafkaUtils.RETRY_TOPIC;

@SpringBootTest
class ConsumerRetryableExceptionIT extends AbstractKafkaIT {

    @Autowired
    private KafkaConsumer<String, byte[]> testConsumer;

    @Autowired
    private KafkaProducer<String, byte[]> testProducer;

    @Autowired
    private TestConsumerAspect testConsumerAspect;

    @MockitoBean
    private PaymentReconciliationServiceRouter paymentReconciliationServiceRouter;

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("steps", () -> 5);
    }

    @BeforeEach
    public void setup() {
        testConsumer.poll(Duration.ofMillis(1000));
    }

    @Test
    void testRepublishToPaymentReconciliationErrorTopicThroughRetryTopics() throws Exception {
        // given
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Encoder encoder = EncoderFactory.get().directBinaryEncoder(outputStream, null);
        DatumWriter<payment_processed> writer = new ReflectDatumWriter<>(payment_processed.class);
        writer.write(TestUtils.getPaymentProcessed(), encoder);

        doThrow(new RetryableException("Retryable exception", new Throwable())).when(paymentReconciliationServiceRouter).route(any());

        // when
        testProducer.send(new ProducerRecord<>(MAIN_TOPIC, 0, System.currentTimeMillis(), "key", outputStream.toByteArray()));
        if (!testConsumerAspect.getLatch().await(20L, TimeUnit.SECONDS)) {
            fail("Timed out waiting for latch");
        }

        // then
        ConsumerRecords<?, ?> consumerRecords = KafkaTestUtils.getRecords(testConsumer, Duration.ofSeconds(1), 6);
        assertThat(KafkaUtils.noOfRecordsForTopic(consumerRecords, MAIN_TOPIC)).isOne();
        assertThat(KafkaUtils.noOfRecordsForTopic(consumerRecords, RETRY_TOPIC)).isEqualTo(4);
        assertThat(KafkaUtils.noOfRecordsForTopic(consumerRecords, ERROR_TOPIC)).isOne();
        assertThat(KafkaUtils.noOfRecordsForTopic(consumerRecords, INVALID_TOPIC)).isZero();
        verify(paymentReconciliationServiceRouter, times(5)).route(any());
    }
}
