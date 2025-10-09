package uk.gov.companieshouse.paymentreconciliation.consumer.serdes;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.Encoder;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.reflect.ReflectDatumWriter;
import org.apache.avro.specific.SpecificDatumWriter;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import payments.payment_processed;
import uk.gov.companieshouse.paymentreconciliation.consumer.exception.InvalidPayloadException;

class PaymentProcessedDeserialiserTest {


    @Test
    void testShouldSuccessfullyDeserialisePaymentProcessed() throws IOException {
        // given
        payment_processed paymentProcessed = new payment_processed();
        paymentProcessed.setAttempt(1);
        paymentProcessed.setRefundId("ref12345");
        paymentProcessed.setPaymentResourceId("12345");
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Encoder encoder = EncoderFactory.get().directBinaryEncoder(outputStream, null);
        DatumWriter<payment_processed> writer = new ReflectDatumWriter<>(payment_processed.class);
        writer.write(paymentProcessed, encoder);
        try (PaymentProcessedDeserialiser deserialiser = new PaymentProcessedDeserialiser()) {
      // when
      payment_processed actual = deserialiser.deserialize("topic", outputStream.toByteArray());

      // then
      assertThat(actual, is(equalTo(paymentProcessed)));
    }
    }

    @Test
    void testDeserialiseDataThrowsInvalidPayloadExceptionIfIOExceptionEncountered() throws IOException {
        // given
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Encoder encoder = EncoderFactory.get().directBinaryEncoder(outputStream, null);
        DatumWriter<String> writer = new SpecificDatumWriter<>(String.class);
        writer.write("hello", encoder);
        try (PaymentProcessedDeserialiser deserialiser = new PaymentProcessedDeserialiser()) {
            // when
            Executable actual = () -> deserialiser.deserialize("topic", outputStream.toByteArray());

            // then
            InvalidPayloadException exception = assertThrows(InvalidPayloadException.class, actual);
            // Note the '\n' is the length prefix of the invalid data sent to the deserialiser
            assertThat(exception.getMessage(), is(equalTo("Invalid payload: [\nhello]")));
            assertThat(exception.getCause(), is(CoreMatchers.instanceOf(IOException.class)));
        }
    }

}