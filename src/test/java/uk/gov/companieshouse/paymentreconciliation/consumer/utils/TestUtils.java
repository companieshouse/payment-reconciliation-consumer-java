package uk.gov.companieshouse.paymentreconciliation.consumer.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;

import payments.payment_processed;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.payments.PaymentDetailsResponse;
import uk.gov.companieshouse.api.payments.PaymentPatchRequestApi;
import uk.gov.companieshouse.api.payments.PaymentResponse;
import uk.gov.companieshouse.api.payments.Refund;

import java.time.OffsetDateTime;
import java.util.List;

public class TestUtils {

    public static final String RESOURCE_LINK = "/transactions/174365-968117-586962/payment";

    public static final String BASE_URL = "http://api-payments.chs.local";

    public static final String GET_URI = "/payments/P9hl8PrKRBk1Zmc";


    public static String getPaymentResponse() throws JsonProcessingException {
        String json = "{\"amount\":\"234.00\",\"completed_at\":\"2022-11-17T07:34:40.778Z\",\"created_at\":\"2022-11-17T07:33:40.204Z\",\"created_by\":{\"email\":\"jnash@companieshouse.gov.uk\",\"forename\":\"\",\"id\":\"Lr_6CHYchIiKAyBQara2GpzhePo\",\"surname\":\"\"},\"description\":\"Overseas Entities Transaction\",\"links\":{\"journey\":\"https://payments.cidev.aws.chdev.org/payments/buwlMOihpdFnWGr/pay\",\"resource\":\"https://api.cidev.aws.chdev.org/transactions/157786-924916-686703/payment\",\"self\":\"payments/buwlMOihpdFnWGr\",\"refunds\":\"\"},\"provider_id\":\"70e8f5ef-79d9-4cdd-a7f0-1fb479b94f4a\",\"payment_method\":\"credit-card\",\"reference\":\"OverseasEntitiesReference_157786-924916-686703\",\"status\":\"expired\",\"costs\":[{\"amount\":\"234.00\",\"available_payment_methods\":[\"credit-card\"],\"class_of_payment\":[\"data-maintenance\"],\"description\":\"Register Overseas Entity fee\",\"description_identifier\":\"description-identifier\",\"product_type\":\"register-overseas-entity\",\"description_values\":{\"Key\":\"Value\"}}],\"etag\":\"8c2536a14aa094ea8acd1043fd11d6ecd16d6871d6bfc90e7244e3e8\",\"kind\":\"payment-session#payment-session\"}\n";
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        PaymentResponse paymentResponse = objectMapper.readValue(json, PaymentResponse.class);
        var apiResponse = new ApiResponse<>(HttpStatus.OK.value(), null, paymentResponse);
        apiResponse.getData().getLinks().setResource("http://localhost:8889" + RESOURCE_LINK);
        return objectMapper.writeValueAsString(apiResponse.getData());
    }

    public static String getPaymentRefundResponse() throws JsonProcessingException {
        String json = "{\"amount\":\"234.00\",\"completed_at\":\"2022-11-17T07:34:40.778Z\",\"created_at\":\"2022-11-17T07:33:40.204Z\",\"created_by\":{\"email\":\"jnash@companieshouse.gov.uk\",\"forename\":\"\",\"id\":\"Lr_6CHYchIiKAyBQara2GpzhePo\",\"surname\":\"\"},\"description\":\"Overseas Entities Transaction\",\"links\":{\"journey\":\"https://payments.cidev.aws.chdev.org/payments/buwlMOihpdFnWGr/pay\",\"resource\":\"https://api.cidev.aws.chdev.org/transactions/157786-924916-686703/payment\",\"self\":\"payments/buwlMOihpdFnWGr\",\"refunds\":\"ref1234\"},\"provider_id\":\"70e8f5ef-79d9-4cdd-a7f0-1fb479b94f4a\",\"payment_method\":\"credit-card\",\"reference\":\"OverseasEntitiesReference_157786-924916-686703\",\"status\":\"expired\",\"costs\":[{\"amount\":\"234.00\",\"available_payment_methods\":[\"credit-card\"],\"class_of_payment\":[\"data-maintenance\"],\"description\":\"Register Overseas Entity fee\",\"description_identifier\":\"description-identifier\",\"product_type\":\"register-overseas-entity\",\"description_values\":{\"Key\":\"Value\"}}],\"etag\":\"8c2536a14aa094ea8acd1043fd11d6ecd16d6871d6bfc90e7244e3e8\",\"kind\":\"payment-session#payment-session\"}\n";
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        PaymentResponse paymentResponse = objectMapper.readValue(json, PaymentResponse.class);


        Refund refund = new Refund();
        refund.setRefundId("ref1234");
        refund.setStatus("submitted");
        paymentResponse.setRefunds(List.of(refund));
        var apiResponse = new ApiResponse<>(HttpStatus.OK.value(), null, paymentResponse);
        apiResponse.getData().getLinks().setResource("http://localhost:8889" + RESOURCE_LINK);
        return objectMapper.writeValueAsString(apiResponse.getData());
    }

    public static String getPaymentDetailsResponse() throws JsonProcessingException {
        String json = "{\"card_type\":\"Visa\",\"external_paymentId\":\"ultllhe38h0434biihf2qms752\",\"transaction_date\":\"2022-11-17T07:33:42.736Z\",\"payment_status\":\"accepted\",\"provider_id\":\"70e8f5ef-79d9-4cdd-a7f0-1fb479b94f4a\"}";
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        PaymentDetailsResponse paymentResponse = objectMapper.readValue(json, PaymentDetailsResponse.class);
        var apiResponse = new ApiResponse<>(HttpStatus.OK.value(), null, paymentResponse);
        return objectMapper.writeValueAsString(apiResponse.getData());
    }

    public static String getLatestRefund() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        Refund refund = new Refund();
        refund.setRefundId("ref1234");
        refund.setStatus("success");

        OffsetDateTime now = OffsetDateTime.now();

        refund.setCreatedAt(now);
        refund.setRefundedAt(now.plusDays(1));

        var apiResponse = new ApiResponse<>(HttpStatus.OK.value(), null, refund);
        return objectMapper.writeValueAsString(apiResponse.getData());
    }


    public static <T> ApiResponse<T> getAPIResponse(T data) {
        return new ApiResponse<>(HttpStatus.OK.value(), null, data);
    }

    public static PaymentPatchRequestApi getPaymentPatchRequestApi() throws JsonProcessingException {
        String json = "{\"completed_at\":\"2025-09-24T06:44:32.354Z\",\"status\":\"paid\",\"reference\":\"Register_ACSP_174365-968117-586962\"}";
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        JsonNode rootNode = objectMapper.readTree(json);
        String status = rootNode.get("status").asText();
        String reference = rootNode.get("reference").asText();
        String paidAt = rootNode.get("completed_at").asText();
        PaymentPatchRequestApi paymentPatchRequestApi = new PaymentPatchRequestApi();
        paymentPatchRequestApi.setPaymentReference(reference);
        paymentPatchRequestApi.setStatus(status);
        paymentPatchRequestApi.setPaidAt(OffsetDateTime.parse(paidAt));
        return paymentPatchRequestApi;
    }

    public static ObjectMapper getObjectMapper() {
        return new ObjectMapper().registerModule(new JavaTimeModule());
    }

    @NotNull
    public static payment_processed getPaymentProcessed() {
        payment_processed paymentProcessed = new payment_processed();
        paymentProcessed.setAttempt(1);
        paymentProcessed.setPaymentResourceId("P9hl8PrKRBk1Zmc");
        return paymentProcessed;
    }

    public static ApiResponse<PaymentResponse> getPaymentResponseRefund() throws JsonProcessingException {
        String json = "{"
                + "\"amount\":\"55.00\","
                + "\"completed_at\":\"2025-09-24T06:44:32.354Z\","
                + "\"created_at\":\"2025-09-24T06:44:27.854Z\","
                + "\"description\":\"Application to register a Companies House authorised agent\","
                + "\"links\":{"
                + "\"journey\":\"https://payments.cidev.aws.chdev.org/payments/Bq286FEk6xzSfXk/pay\","
                + "\"resource\":\"" + RESOURCE_LINK + "\","
                + "\"self\":\"payments/Bq286FEk6xzSfXk\""
                + "},"
                + "\"payment_method\":\"credit-card\","
                + "\"reference\":\"Register_ACSP_174365-968117-586962\","
                + "\"status\":\"paid\","
                + "\"etag\":\"34e92e90a981a9686b45a56204e98d7d1fef86bbb446bf0c2cf5c679\","
                + "\"kind\":\"payment-session#payment-session\","
                + "\"refunds\":["
                + "{"
                + "\"refund_id\":\"R123\","
                + "\"created_at\":\"2025-09-23T10:15:30.000Z\","
                + "\"amount\":1000,"
                + "\"status\":\"approved\","
                + "\"external_refund_url\":\"https://example.com/refund/R123\","
                + "\"refund_reference\":\"REF123\""
                + "},"
                + "{"
                + "\"refund_id\":\"R124\","
                + "\"created_at\":\"2025-09-22T09:10:25.000Z\","
                + "\"amount\":500,"
                + "\"status\":\"pending\","
                + "\"external_refund_url\":\"https://example.com/refund/R124\","
                + "\"refund_reference\":\"REF124\""
                + "}"
                + "]"
                + "}";
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        PaymentResponse paymentResponse = objectMapper.readValue(json, PaymentResponse.class);
        return getAPIResponse(paymentResponse);
    }
}
