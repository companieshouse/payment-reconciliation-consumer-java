package uk.gov.companieshouse.paymentreconciliation.consumer.serdes;

import static uk.gov.companieshouse.paymentreconciliation.consumer.Application.NAMESPACE;

import java.io.IOException;

import org.apache.avro.AvroRuntimeException;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.reflect.ReflectDatumReader;
import org.apache.kafka.common.serialization.Deserializer;

import payments.payment_processed;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.paymentreconciliation.consumer.exception.InvalidPayloadException;
import uk.gov.companieshouse.paymentreconciliation.consumer.logging.DataMapHolder;

public class PaymentProcessedDeserialiser implements Deserializer<payment_processed> {

    private static final Logger LOGGER = LoggerFactory.getLogger(NAMESPACE);

    @Override
    public payment_processed deserialize(String topic, byte[] data) {
        try {
            Decoder decoder = DecoderFactory.get().binaryDecoder(data, null);
            DatumReader<payment_processed> reader = new ReflectDatumReader<>(payment_processed.class);
            return reader.read(null, decoder);
        } catch (IOException | AvroRuntimeException ex) {
            String payload = new String(data);
            LOGGER.error("Error deserialising message payload: [%s]".formatted(payload), ex, DataMapHolder.getLogMap());
            throw new InvalidPayloadException("Invalid payload: [%s]".formatted(payload), ex);
        }
    }
}
