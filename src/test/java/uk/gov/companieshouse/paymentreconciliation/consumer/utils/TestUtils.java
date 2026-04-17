package uk.gov.companieshouse.paymentreconciliation.consumer.utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.jetbrains.annotations.NotNull;
import org.testcontainers.shaded.org.apache.commons.io.IOUtils;

import payments.payment_processed;

public class TestUtils {

    public static final String BASE_URL = "http://api-payments.chs.local";

    public static final String GET_URI = "/payments/P9hl8PrKRBk1Zmc";

    public static String getPaymentResponse() throws IOException {
        return IOUtils.resourceToString("/apiResponses/paymentResponse.json", StandardCharsets.UTF_8);
    }

    public static String getPaymentRefundResponse() throws IOException {
        return IOUtils.resourceToString("/apiResponses/paymentRefundResponse.json", StandardCharsets.UTF_8);
    }

    public static String getPaymentDetailsResponse() throws IOException {
        return IOUtils.resourceToString("/apiResponses/paymentDetailsResponse.json", StandardCharsets.UTF_8);
    }

    public static String getPaymentSensitiveResponse() throws IOException {
        return IOUtils.resourceToString("/apiResponses/paymentSensitiveResponse.json", StandardCharsets.UTF_8);
    }

    public static String getPaymentDetailsSensitiveResponse() throws IOException {
        return IOUtils.resourceToString("/apiResponses/paymentDetailsSensitiveResponse.json", StandardCharsets.UTF_8);
    }

    public static String getLatestRefund() throws IOException {
        return IOUtils.resourceToString("/apiResponses/refundPatchResponse.json", StandardCharsets.UTF_8);
    }

    @NotNull
    public static payment_processed getPaymentProcessed() {
        payment_processed paymentProcessed = new payment_processed();
                paymentProcessed.setAttempt(1);
                paymentProcessed.setPaymentResourceId("P9hl8PrKRBk1Zmc");
        return paymentProcessed;
    }
}
