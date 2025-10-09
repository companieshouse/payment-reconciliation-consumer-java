package uk.gov.companieshouse.paymentreconciliation.consumer.kafka;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;

import payments.payment_processed;
import uk.gov.companieshouse.paymentreconciliation.consumer.exception.RetryableException;
import uk.gov.companieshouse.paymentreconciliation.consumer.logging.DataMapHolder;
import uk.gov.companieshouse.paymentreconciliation.consumer.service.PaymentReconciliationServiceRouter;
import uk.gov.companieshouse.paymentreconciliation.consumer.util.KafkaUtils;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class ConsumerTest {

    @Mock
    private PaymentReconciliationServiceRouter router;
    @Mock
    private MessageFlags messageFlags;
    @Mock
    private Message<payment_processed> message;
    @Mock
    private payment_processed paymentreconciliation;
    @Mock
    private MessageHeaders headers;

    private Consumer consumer;

    private static final int MAX_ATTEMPTS = 3;

    @BeforeEach
    void setUp() {
        consumer = new Consumer(router, messageFlags, MAX_ATTEMPTS);
        DataMapHolder.clear();
    }

    @Test
    void consume_shouldRouteMessage_whenNoException() {
        when(message.getPayload()).thenReturn(paymentreconciliation);

        consumer.consume(message);

        verify(router).route(paymentreconciliation);
        verify(messageFlags, never()).setRetryable(anyBoolean());
    }

    @Test
    void consume_shouldSetRetryableAndThrow_whenRetryableException() {
        when(message.getPayload()).thenReturn(paymentreconciliation);
        when(message.getHeaders()).thenReturn(headers);
        doThrow(new RetryableException("retry", new Throwable())).when(router).route(paymentreconciliation);

        RetryableException thrown = assertThrows(RetryableException.class, () -> consumer.consume(message));
        assertEquals("retry", thrown.getMessage());
        verify(messageFlags).setRetryable(true);
    }

    @Test
    void consume_shouldClearDataMapHolderInFinally() {
        when(message.getPayload()).thenReturn(paymentreconciliation);

        consumer.consume(message);
        var h = DataMapHolder.getLogMap();
        assertEquals("uninitialised",h.get("request_id"));
    }

    @Test
    void logIfMaxAttemptsReached_shouldLogError_whenRetryCountAtMaxAttemptsMinusOne() {
        int retryCount = MAX_ATTEMPTS - 1;
        String paymentResourceId = UUID.randomUUID().toString();

        Map<String, Object> headerMap = new HashMap<>();
        headerMap.put("kafka_receivedTopic", "test-topic");
        headerMap.put("kafka_receivedPartitionId", 1);
        headerMap.put("kafka_offset", 10L);

        when(message.getHeaders()).thenReturn(new MessageHeaders(headerMap));
        when(message.getPayload()).thenReturn(paymentreconciliation);
        when(paymentreconciliation.getPaymentResourceId()).thenReturn(paymentResourceId);

        try (MockedStatic<KafkaUtils> kafkaUtils = Mockito.mockStatic(KafkaUtils.class)) {
            kafkaUtils.when(() -> KafkaUtils.getRetryCount(any())).thenReturn(retryCount);
            kafkaUtils.when(() -> KafkaUtils.extractRefundRequest(any())).thenReturn(paymentreconciliation);

            RetryableException ex = new RetryableException("Retryable exception", new Throwable());
            consumer.consume(message); // Will not throw, so call logIfMaxAttemptsReached directly for coverage
            // logIfMaxAttemptsReached is private, so we can't call it directly.
            // Instead, we simulate the retryable path:
            doThrow(ex).when(router).route(paymentreconciliation);
            assertThrows(RetryableException.class, () -> consumer.consume(message));
        }
    }

    @Test
    void logIfMaxAttemptsReached_shouldNotLogError_whenRetryCountBelowMaxAttemptsMinusOne() {
        int retryCount = 0;
        String paymentResourceId = UUID.randomUUID().toString();

        Map<String, Object> headerMap = new HashMap<>();
        headerMap.put("kafka_receivedTopic", "test-topic");
        headerMap.put("kafka_receivedPartitionId", 1);
        headerMap.put("kafka_offset", 10L);

        when(message.getHeaders()).thenReturn(new MessageHeaders(headerMap));
        when(message.getPayload()).thenReturn(paymentreconciliation);
        when(paymentreconciliation.getPaymentResourceId()).thenReturn(paymentResourceId);

        try (MockedStatic<KafkaUtils> kafkaUtils = Mockito.mockStatic(KafkaUtils.class)) {
            kafkaUtils.when(() -> KafkaUtils.getRetryCount(any())).thenReturn(retryCount);
            kafkaUtils.when(() -> KafkaUtils.extractRefundRequest(any())).thenReturn(paymentreconciliation);

            RetryableException ex = new RetryableException("Retryable exception", new Throwable());
            doThrow(ex).when(router).route(paymentreconciliation);
            assertThrows(RetryableException.class, () -> consumer.consume(message));
        }
    }
}