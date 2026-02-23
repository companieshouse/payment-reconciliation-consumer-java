package uk.gov.companieshouse.paymentreconciliation.consumer.serdes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

import java.io.IOException;

import org.apache.avro.io.DatumWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import payments.payment_processed;
import uk.gov.companieshouse.paymentreconciliation.consumer.exception.NonRetryableException;

@ExtendWith(MockitoExtension.class)
class PaymentProcessedSerializerTest {

    private PaymentProcessedSerializer serializer;

    @Mock
    private DatumWriter<payment_processed> mockDatumWriter;

    @BeforeEach
    void setUp() {
        serializer = new PaymentProcessedSerializer();
    }

    @Test
    void serialize_ReturnsNonNullByteArray_WhenValidData() {
        payment_processed data = new payment_processed();
        data.setAttempt(1);
        data.setRefundId("ref12345");
        data.setPaymentResourceId("12345");
        byte[] result = serializer.serialize("test-topic", data);
        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    @Test
    void serialize_ThrowsNonRetryableException_WhenWriterThrowsIOException() throws IOException {
        payment_processed data = new payment_processed();

        PaymentProcessedSerializer spySerializer = Mockito.spy(serializer);
        doReturn(mockDatumWriter).when(spySerializer).getDatumWriter();
        doThrow(new IOException("Test IO Exception")).when(mockDatumWriter).write(any(), any());

        NonRetryableException ex = assertThrows(
                NonRetryableException.class,
                () -> spySerializer.serialize("test-topic", data)
        );
        assertTrue(ex.getMessage().contains("Error serialising refund request"));
        assertTrue(ex.getCause() instanceof IOException);
    }

    @Test
    void getDatumWriter_ReturnsReflectDatumWriter() {
        DatumWriter<payment_processed> writer = serializer.getDatumWriter();
        assertNotNull(writer);
        assertEquals("org.apache.avro.reflect.ReflectDatumWriter", writer.getClass().getName());
    }
}