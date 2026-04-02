package uk.gov.companieshouse.paymentreconciliation.consumer.kafka;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.patch;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static uk.gov.companieshouse.paymentreconciliation.consumer.kafka.KafkaUtils.MAIN_TOPIC;
import static uk.gov.companieshouse.paymentreconciliation.consumer.utils.TestUtils.GET_URI;
import static uk.gov.companieshouse.paymentreconciliation.consumer.utils.TestUtils.getPaymentProcessed;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.Encoder;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.reflect.ReflectDatumWriter;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.shaded.org.apache.commons.io.IOUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;

import payments.payment_processed;
import uk.gov.companieshouse.paymentreconciliation.consumer.config.ProductCodeLoader;
import uk.gov.companieshouse.paymentreconciliation.consumer.model.EshuDao;
import uk.gov.companieshouse.paymentreconciliation.consumer.model.PaymentTransactionsResourceDao;
import uk.gov.companieshouse.paymentreconciliation.consumer.model.RefundDao;
import uk.gov.companieshouse.paymentreconciliation.consumer.repository.EshuRepository;
import uk.gov.companieshouse.paymentreconciliation.consumer.repository.RefundRepository;
import uk.gov.companieshouse.paymentreconciliation.consumer.repository.TransactionRepository;
import uk.gov.companieshouse.paymentreconciliation.consumer.utils.TestUtils;

@SpringBootTest(properties = { "payments.api-url=http://localhost:8889" })
@WireMockTest(httpPort = 8889)
class ConsumerPositiveIT extends AbstractKafkaIT {

    @Autowired
    private KafkaConsumer<String, byte[]> testConsumer;

    @Autowired
    private KafkaProducer<String, byte[]> testProducer;

    @Autowired
    private TestConsumerAspect testConsumerAspect;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private EshuRepository eshuRepository;

    @Autowired
    private RefundRepository refundRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private ProductCodeLoader productCodeLoader;

    @Container
    private static final MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:6.0.13");

    private static final String ESHU_COLLECTION = "eshu";
    private static final String TRANSACTION_COLLECTION = "payment_transaction";
    private static final String REFUND_COLLECTION = "refunds";

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("steps", () -> 1);
        registry.add("spring.mongodb.uri", () -> mongoDBContainer.getReplicaSetUrl("payment_reconciliation") );
        registry.add("spring.mongodb.database", () -> "payment_reconciliation");
    }

    @BeforeEach
    void setup() {
        testConsumerAspect.resetLatch();
        testConsumer.poll(Duration.ofMillis(1000));
        mongoTemplate.createCollection(ESHU_COLLECTION);
        mongoTemplate.createCollection(TRANSACTION_COLLECTION);
        mongoTemplate.createCollection(REFUND_COLLECTION);
    }

    @AfterEach
    void cleanUpMongo() {
        mongoTemplate.dropCollection(ESHU_COLLECTION);
        mongoTemplate.dropCollection(TRANSACTION_COLLECTION);
        mongoTemplate.dropCollection(REFUND_COLLECTION);
    }

    @Test
    void shouldConsumePaymentProcessMessageStandardTransactionSuccessfully() throws Exception {
        // Arrange
        byte[] message = createPaymentProcessedMessage();
        stubPaymentApiResponses();

        // Act
        sendMessageToKafka(MAIN_TOPIC, message);
        awaitLatchOrFail(5);

        // Assert
        PaymentTransactionsResourceDao transactionDocumentFromMongo = transactionRepository.findAll().getFirst();
        EshuDao eshuDocumentFromMongo = eshuRepository.findAll().getFirst();

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

        String transactionDocumentJson = IOUtils.resourceToString("/mongoDb/payment_transaction.json",
                StandardCharsets.UTF_8);
        String eshuDocumentJson = IOUtils.resourceToString("/mongoDb/eshu.json", StandardCharsets.UTF_8);
        EshuDao expectedEshu = objectMapper.readValue(eshuDocumentJson, EshuDao.class);
        PaymentTransactionsResourceDao expectedTransaction = objectMapper.readValue(transactionDocumentJson,
                PaymentTransactionsResourceDao.class);

        assertThat(transactionDocumentFromMongo).usingRecursiveComparison().isEqualTo(expectedTransaction);
        assertThat(eshuDocumentFromMongo).usingRecursiveComparison().isEqualTo(expectedEshu);
    }

    @Test
    void shouldConsumePaymentProcessMessageRefundTransactionSuccessfully() throws Exception {
        // Arrange
        byte[] message = createRefundPaymentProcessedMessage();
        stubRefundPaymentApiResponses();

        // Act
        sendMessageToKafka(MAIN_TOPIC, message);
        awaitLatchOrFail(5);

        // Assert
        RefundDao refundDocumentFromMongo = refundRepository.findAll().getFirst();

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

        String refundDocumentJson = IOUtils.resourceToString("/mongoDb/refund.json", StandardCharsets.UTF_8);
        RefundDao expectedRefund = objectMapper.readValue(refundDocumentJson, RefundDao.class);

        assertThat(refundDocumentFromMongo).usingRecursiveComparison().isEqualTo(expectedRefund);
    }

    private byte[] createPaymentProcessedMessage() throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Encoder encoder = EncoderFactory.get().directBinaryEncoder(outputStream, null);
        DatumWriter<payment_processed> writer = new ReflectDatumWriter<>(payment_processed.class);
        writer.write(getPaymentProcessed(), encoder);
        return outputStream.toByteArray();
    }

    private byte[] createRefundPaymentProcessedMessage() throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Encoder encoder = EncoderFactory.get().directBinaryEncoder(outputStream, null);
        DatumWriter<payment_processed> writer = new ReflectDatumWriter<>(payment_processed.class);
        payment_processed refundPayment = getPaymentProcessed();
        refundPayment.setRefundId("ref1234");
        writer.write(refundPayment, encoder);
        return outputStream.toByteArray();
    }

    private void stubPaymentApiResponses() throws IOException {
        stubFor(get(GET_URI)
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(TestUtils.getPaymentResponse())));
        stubFor(get("/private/payments/P9hl8PrKRBk1Zmc/payment-details")
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(TestUtils.getPaymentDetailsResponse())));
    }

    private void stubRefundPaymentApiResponses() throws IOException {
        stubFor(get(GET_URI)
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(TestUtils.getPaymentRefundResponse())));
        stubFor(get("/private/payments/P9hl8PrKRBk1Zmc/payment-details")
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(TestUtils.getPaymentDetailsResponse())));
        stubFor(patch(("/payments/P9hl8PrKRBk1Zmc/refunds/ref1234"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(TestUtils.getLatestRefund())));
    }

    private void sendMessageToKafka(String topic, byte[] message) {
        testProducer.send(new ProducerRecord<>(topic, 0, System.currentTimeMillis(), "key", message));
    }

    private void awaitLatchOrFail(int seconds) throws InterruptedException {
        if (!testConsumerAspect.getLatch().await(seconds, TimeUnit.SECONDS)) {
            fail("Timed out waiting for latch");
        }
    }
}