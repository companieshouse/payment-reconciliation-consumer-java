package uk.gov.companieshouse.paymentreconciliation.consumer.kafka;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.patch;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static uk.gov.companieshouse.paymentreconciliation.consumer.kafka.KafkaUtils.ERROR_TOPIC;
import static uk.gov.companieshouse.paymentreconciliation.consumer.kafka.KafkaUtils.INVALID_TOPIC;
import static uk.gov.companieshouse.paymentreconciliation.consumer.kafka.KafkaUtils.MAIN_TOPIC;
import static uk.gov.companieshouse.paymentreconciliation.consumer.kafka.KafkaUtils.RETRY_TOPIC;
import static uk.gov.companieshouse.paymentreconciliation.consumer.utils.TestUtils.GET_URI;
import static uk.gov.companieshouse.paymentreconciliation.consumer.utils.TestUtils.getPaymentProcessed;

import java.io.ByteArrayOutputStream;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

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

import com.github.tomakehurst.wiremock.junit5.WireMockTest;

import payments.payment_processed;
import uk.gov.companieshouse.paymentreconciliation.consumer.repository.EshuRepository;
import uk.gov.companieshouse.paymentreconciliation.consumer.repository.RefundRepository;
import uk.gov.companieshouse.paymentreconciliation.consumer.repository.TransactionRepository;
import uk.gov.companieshouse.paymentreconciliation.consumer.utils.TestUtils;

@SpringBootTest(properties = {
        "payments.api-url=http://localhost:8889",
})
@WireMockTest(httpPort = 8889)
class ConsumerPositiveIT extends AbstractKafkaIT {

    @Autowired
    private KafkaConsumer<String, byte[]> testConsumer;
    @Autowired
    private KafkaProducer<String, byte[]> testProducer;
    @Autowired
    private TestConsumerAspect testConsumerAspect;

    @MockitoBean
    private TransactionRepository transactionRepository;

    @MockitoBean
    private EshuRepository eshuRepository;

    @MockitoBean
    private RefundRepository refundRepository;

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("steps", () -> 1);
    }

    @BeforeEach
    public void setup() {
        testConsumerAspect.resetLatch();
        testConsumer.poll(Duration.ofMillis(1000));
    }

    @Test
    void shouldConsumePaymentProcessMessageStandardTransactionSuccessfully() throws Exception {

        // given
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Encoder encoder = EncoderFactory.get().directBinaryEncoder(outputStream, null);
        DatumWriter<payment_processed> writer = new ReflectDatumWriter<>(payment_processed.class);
        writer.write(getPaymentProcessed(), encoder);
        stubFor(get(GET_URI)
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(TestUtils.getPaymentResponse())
                ));

        stubFor(get("/private/payments/P9hl8PrKRBk1Zmc/payment-details")
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(TestUtils.getPaymentDetailsResponse())
                ));

        // when
        testProducer.send(new ProducerRecord<>(MAIN_TOPIC, 0, System.currentTimeMillis(),
                "key", outputStream.toByteArray()));
        if (!testConsumerAspect.getLatch().await(5, TimeUnit.SECONDS)) {
            fail("Timed out waiting for latch");
        }
        ConsumerRecords<?, ?> consumerRecords = KafkaTestUtils.getRecords(testConsumer, Duration.ofMillis(10000L), 1);
        assertThat(KafkaUtils.noOfRecordsForTopic(consumerRecords, RETRY_TOPIC)).isZero();
        assertThat(KafkaUtils.noOfRecordsForTopic(consumerRecords, ERROR_TOPIC)).isZero();
        assertThat(KafkaUtils.noOfRecordsForTopic(consumerRecords, INVALID_TOPIC)).isZero();
        verify(getRequestedFor(urlEqualTo(GET_URI)));
        verify(getRequestedFor(urlEqualTo("/private"+GET_URI+"/payment-details")));

        org.mockito.Mockito.verify(eshuRepository).saveAll(anyList());
        org.mockito.Mockito.verify(transactionRepository).saveAll(anyList());
    }

    @Test
    void shouldConsumePaymentProcessMessageRefundTransactionSuccessfully() throws Exception {

        // given
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Encoder encoder = EncoderFactory.get().directBinaryEncoder(outputStream, null);
        DatumWriter<payment_processed> writer = new ReflectDatumWriter<>(payment_processed.class);

        payment_processed refundPayment  = getPaymentProcessed();
        refundPayment.setRefundId("ref1234");

        writer.write(refundPayment, encoder);
        stubFor(get(GET_URI)
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(TestUtils.getPaymentRefundResponse())
                ));

        stubFor(get("/private/payments/P9hl8PrKRBk1Zmc/payment-details")
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(TestUtils.getPaymentDetailsResponse())
                ));

        stubFor(patch(("/payments/P9hl8PrKRBk1Zmc/refunds/ref1234"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(TestUtils.getLatestRefund())

                ));

        // when
        testProducer.send(new ProducerRecord<>(MAIN_TOPIC, 0, System.currentTimeMillis(),
                "key", outputStream.toByteArray()));
        if (!testConsumerAspect.getLatch().await(20, TimeUnit.SECONDS)) {
            fail("Timed out waiting for latch");
        }
        ConsumerRecords<?, ?> consumerRecords = KafkaTestUtils.getRecords(testConsumer, Duration.ofMillis(10000L), 1);
        assertThat(KafkaUtils.noOfRecordsForTopic(consumerRecords, RETRY_TOPIC)).isZero();
        assertThat(KafkaUtils.noOfRecordsForTopic(consumerRecords, ERROR_TOPIC)).isZero();
        assertThat(KafkaUtils.noOfRecordsForTopic(consumerRecords, INVALID_TOPIC)).isZero();
        verify(getRequestedFor(urlEqualTo(GET_URI)));
        verify(getRequestedFor(urlEqualTo("/private"+GET_URI+"/payment-details")));

        org.mockito.Mockito.verify(refundRepository).save(any());
    }


}