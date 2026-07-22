package uk.gov.companieshouse.paymentreconciliation.consumer.utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.jetbrains.annotations.NotNull;
import org.testcontainers.shaded.org.apache.commons.io.IOUtils;

import payments.payment_processed;

public class TestUtils {

    public static final String GET_URI = "/payments/P9hl8PrKRBk1Zmc";

    public static String getPaymentResponse() throws IOException {
        return IOUtils.resourceToString("/apiResponses/payment-session-govpay-refund-response.json",
                StandardCharsets.UTF_8);
    }

    public static String getPaymentRefundResponse() throws IOException {
        return IOUtils.resourceToString("/apiResponses/payment-session-standard-with-1dp-completed-at-response.json",
                StandardCharsets.UTF_8);
    }

    public static String getPaymentDetailsResponse() throws IOException {
        return IOUtils.resourceToString("/apiResponses/payment-details-standard-response.json",
                StandardCharsets.UTF_8);
    }

    public static String getPaymentSensitiveResponse() throws IOException {
        return IOUtils.resourceToString("/apiResponses/payment-session-sensitive-product-response.json",
                StandardCharsets.UTF_8);
    }

    public static String getPaymentDetailsSensitiveResponse() throws IOException {
        return IOUtils.resourceToString("/apiResponses/payment-details-sensitive-response.json",
                StandardCharsets.UTF_8);
    }

    public static String getLatestRefund() throws IOException {
        return IOUtils.resourceToString("/apiResponses/refund-patch-success-response.json",
                StandardCharsets.UTF_8);
    }

    @NotNull
    public static payment_processed getPaymentProcessed() {
        payment_processed paymentProcessed = new payment_processed();
        paymentProcessed.setAttempt(1);
        paymentProcessed.setPaymentResourceId("P9hl8PrKRBk1Zmc");
        return paymentProcessed;
    }
}
