package uk.gov.companieshouse.paymentreconciliation.consumer.kafka;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class InvalidMessageRouterTest {

    private InvalidMessageRouter router;
    private MessageFlags messageFlags;
    private final String invalidTopic = "invalid-topic";
    private final String originalTopic = "original-topic";
    private final String key = "test-key";
    private final String value = "test-value";
    private final BigInteger partition = BigInteger.valueOf(2);
    private final BigInteger offset = BigInteger.valueOf(42);
    private final String exceptionMessage = "some error";

    @BeforeEach
    void setUp() {
        router = new InvalidMessageRouter();
        messageFlags = mock(MessageFlags.class);
        Map<String, Object> configs = new HashMap<>();
        configs.put("message-flags", messageFlags);
        configs.put("invalid-topic", invalidTopic);
        router.configure(configs);
    }

    @Test
    void onSend_whenRetryable_returnsOriginalRecordAndDestroysFlag() {
        ProducerRecord<String, Object> producerRecord = new ProducerRecord<>(originalTopic, key, value);
        when(messageFlags.isRetryable()).thenReturn(true);

        ProducerRecord<String, Object> result = router.onSend(producerRecord);

        assertSame(producerRecord, result);
        verify(messageFlags).destroy();
    }

    @Test
    void onSend_whenNotRetryable_returnsRecordWithInvalidTopic_andLogsError() {
        Headers headers = new RecordHeaders();
        headers.add("kafka_originalTopic", originalTopic.getBytes(StandardCharsets.UTF_8));
        headers.add("kafka_originalPartition", partition.toByteArray());
        headers.add("kafka_originalOffset", offset.toByteArray());
        headers.add("kafka_exceptionMessage", exceptionMessage.getBytes(StandardCharsets.UTF_8));
        ProducerRecord<String, Object> productRecord = new ProducerRecord<>(originalTopic, null, key, value, headers);

        when(messageFlags.isRetryable()).thenReturn(false);

        Map<String, Object> configs = new HashMap<>();
        configs.put("message-flags", messageFlags);
        configs.put("invalid-topic", invalidTopic);
        router.configure(configs);

        ProducerRecord<String, Object> result = router.onSend(productRecord);

        assertEquals(invalidTopic, result.topic());
        assertEquals(key, result.key());
        assertEquals(value, result.value());
    }

    @Test
    void onSend_whenHeadersMissing_usesDefaults() {
        ProducerRecord<String, Object> producerRecord = new ProducerRecord<>(originalTopic, key, value);
        when(messageFlags.isRetryable()).thenReturn(false);

        Map<String, Object> configs = new HashMap<>();
        configs.put("message-flags", messageFlags);
        configs.put("invalid-topic", invalidTopic);
        router.configure(configs);

        ProducerRecord<String, Object> result = router.onSend(producerRecord);

        assertEquals(invalidTopic, result.topic());
        assertEquals(key, result.key());
        assertEquals(value, result.value());
    }

    @Test
    void configure_setsFieldsCorrectly() {
        InvalidMessageRouter newRouter = new InvalidMessageRouter();
        Map<String, Object> configs = new HashMap<>();
        MessageFlags flags = mock(MessageFlags.class);
        configs.put("message-flags", flags);
        configs.put("invalid-topic", "foo");
        newRouter.configure(configs);

        // Use reflection to check private fields
        assertDoesNotThrow(() -> {
            var mf = newRouter.getClass().getDeclaredField("messageFlags");
            mf.setAccessible(true);
            assertSame(flags, mf.get(newRouter));
            var it = newRouter.getClass().getDeclaredField("invalidTopic");
            it.setAccessible(true);
            assertEquals("foo", it.get(newRouter));
        });
    }
}