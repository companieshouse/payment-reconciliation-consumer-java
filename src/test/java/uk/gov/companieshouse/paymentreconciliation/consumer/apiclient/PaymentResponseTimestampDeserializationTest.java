package uk.gov.companieshouse.paymentreconciliation.consumer.apiclient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.testcontainers.shaded.org.apache.commons.io.IOUtils;
import tools.jackson.databind.DatabindException;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.json.JsonMapper;
import uk.gov.companieshouse.api.model.payment.PaymentResponse;

class PaymentResponseTimestampDeserializationTest {

    private static final JsonMapper OBJECT_MAPPER = JsonMapper.builder()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .build();

    @Test
    void shouldDeserializePaymentResponseWithThreeDigitMilliseconds() throws Exception {
        String payload = IOUtils.resourceToString("/apiResponses/payment-session-completed-at-3dp-response.json",
                StandardCharsets.UTF_8);

        PaymentResponse paymentResponse = OBJECT_MAPPER.readValue(payload, PaymentResponse.class);

        assertEquals(Instant.parse("2026-07-22T10:00:02.640Z"), paymentResponse.getCompletedAt());
        assertEquals(Instant.parse("2026-07-22T10:00:02.640Z"), paymentResponse.getCreatedAt());
    }

    @ParameterizedTest
    @CsvSource({
            "/apiResponses/payment-session-completed-at-1dp-response.json,2026-07-22T10:00:02.100Z",
            "/apiResponses/payment-session-completed-at-2dp-response.json,2026-07-22T10:00:02.640Z",
            "/apiResponses/payment-session-completed-at-3dp-response.json,2026-07-22T10:00:02.640Z"
    })
    void shouldDeserializePaymentResponseAcrossVariableMillisecondPrecision(String fixturePath, String expectedInstant)
            throws Exception {
        String payload = IOUtils.resourceToString(fixturePath, StandardCharsets.UTF_8);

        PaymentResponse paymentResponse = OBJECT_MAPPER.readValue(payload, PaymentResponse.class);
        Instant expected = Instant.parse(expectedInstant);

        assertEquals(expected, paymentResponse.getCompletedAt());
        assertEquals(expected, paymentResponse.getCreatedAt());
    }

    @Test
    void shouldFailToDeserializePaymentResponseForMalformedTimestamp() {
        String payload = "{ \"completed_at\": \"2026-07-22 10:00:02.64Z\" }";

        assertThrows(DatabindException.class, () -> OBJECT_MAPPER.readValue(payload, PaymentResponse.class));
    }
}
