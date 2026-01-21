package uk.gov.companieshouse.paymentreconciliation.consumer.util;

import uk.gov.companieshouse.api.model.payment.PaymentResponse;
import uk.gov.companieshouse.paymentreconciliation.consumer.config.ProductCodeLoader;

public class PaymentUtils {

    private static final int PROTECT_DETAILS = 16800;

    private PaymentUtils() {}

    public static void maskSensitiveFields(PaymentResponse paymentSession, ProductCodeLoader productCodeLoader) {
        String productType = paymentSession.getCosts().getFirst().getProductType();
        int productCode = productCodeLoader.getProductCodes().get(productType);
        if (productCode == PROTECT_DETAILS) {
            paymentSession.getCreatedBy().setEmail("");
            paymentSession.setCompanyNumber("");
        }
    }
}
