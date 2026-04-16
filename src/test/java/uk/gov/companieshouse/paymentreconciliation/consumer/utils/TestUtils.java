package uk.gov.companieshouse.paymentreconciliation.consumer.utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.testcontainers.shaded.org.apache.commons.io.IOUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import payments.payment_processed;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.payment.PaymentResponse;
import uk.gov.companieshouse.api.model.payment.RefundModel;
import uk.gov.companieshouse.api.payments.PaymentDetailsResponse;

public class TestUtils {

    public static final String BASE_URL = "http://api-payments.chs.local";

    public static final String GET_URI = "/payments/P9hl8PrKRBk1Zmc";

    public static String getPaymentResponse() throws IOException {
        String json = IOUtils.resourceToString("/apiResponses/paymentResponse.json", StandardCharsets.UTF_8);
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        PaymentResponse paymentResponse = objectMapper.readValue(json, PaymentResponse.class);
        var apiResponse = new ApiResponse<>(HttpStatus.OK.value(), null, paymentResponse);
        return objectMapper.writeValueAsString(apiResponse.getData());
    }

    public static String getPaymentRefundResponse() throws IOException {
        String json = IOUtils.resourceToString("/apiResponses/paymentRefundResponse.json", StandardCharsets.UTF_8);
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        PaymentResponse paymentResponse = objectMapper.readValue(json, PaymentResponse.class);
        var apiResponse = new ApiResponse<>(HttpStatus.OK.value(), null, paymentResponse);
        return objectMapper.writeValueAsString(apiResponse.getData());
    }

    public static String getPaymentDetailsResponse() throws IOException {
        String json = IOUtils.resourceToString("/apiResponses/paymentDetailsResponse.json", StandardCharsets.UTF_8);
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        PaymentDetailsResponse paymentResponse = objectMapper.readValue(json, PaymentDetailsResponse.class);
        var apiResponse = new ApiResponse<>(HttpStatus.OK.value(), null, paymentResponse);
        return objectMapper.writeValueAsString(apiResponse.getData());
    }

    public static String getPaymentSensitiveResponse() throws IOException {
        String json = IOUtils.resourceToString("/apiResponses/paymentSensitiveResponse.json", StandardCharsets.UTF_8);
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        PaymentResponse paymentResponse = objectMapper.readValue(json, PaymentResponse.class);
        var apiResponse = new ApiResponse<>(HttpStatus.OK.value(), null, paymentResponse);
        return objectMapper.writeValueAsString(apiResponse.getData());
    }

    public static String getPaymentDetailsSensitiveResponse() throws IOException {
        String json = IOUtils.resourceToString("/apiResponses/paymentDetailsSensitiveResponse.json", StandardCharsets.UTF_8);
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        PaymentDetailsResponse paymentResponse = objectMapper.readValue(json, PaymentDetailsResponse.class);
        var apiResponse = new ApiResponse<>(HttpStatus.OK.value(), null, paymentResponse);
        return objectMapper.writeValueAsString(apiResponse.getData());
    }

    public static String getLatestRefund() throws IOException {
        String json = IOUtils.resourceToString("/apiResponses/refundPatchResponse.json", StandardCharsets.UTF_8);
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        RefundModel refundModel = objectMapper.readValue(json, RefundModel.class);
        var apiResponse = new ApiResponse<>(HttpStatus.OK.value(), null, refundModel);
        return objectMapper.writeValueAsString(apiResponse.getData());
    }

    @NotNull
    public static payment_processed getPaymentProcessed() {
        payment_processed paymentProcessed = new payment_processed();
                paymentProcessed.setAttempt(1);
                paymentProcessed.setPaymentResourceId("P9hl8PrKRBk1Zmc");
        return paymentProcessed;
    }
}
