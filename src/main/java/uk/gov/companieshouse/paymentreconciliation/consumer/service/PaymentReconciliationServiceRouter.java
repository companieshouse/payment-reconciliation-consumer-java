package uk.gov.companieshouse.paymentreconciliation.consumer.service;

import static uk.gov.companieshouse.paymentreconciliation.consumer.Application.NAMESPACE;

import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import org.springframework.stereotype.Component;

import payments.payment_processed;
import uk.gov.companieshouse.api.payments.PaymentDetailsResponse;
import uk.gov.companieshouse.api.payments.PaymentResponse;
import uk.gov.companieshouse.paymentreconciliation.consumer.apiclient.PaymentsApiClient;
import uk.gov.companieshouse.paymentreconciliation.consumer.config.ProductCodeLoader;
import uk.gov.companieshouse.paymentreconciliation.consumer.service.handler.RefundTransactionHandler;
import uk.gov.companieshouse.paymentreconciliation.consumer.service.handler.StandardTransactionHandler;
import uk.gov.companieshouse.paymentreconciliation.consumer.util.PaymentUtils;

@Component
public class PaymentReconciliationServiceRouter {

    private final ProductCodeLoader productCodeLoader;
    private final PaymentsApiClient paymentRefundApiClient;
    private final StandardTransactionHandler standardTransactionHandler;
    private final RefundTransactionHandler refundTransactionHandler;
    private static final Logger LOGGER = LoggerFactory.getLogger(NAMESPACE);

    public PaymentReconciliationServiceRouter(PaymentsApiClient paymentRefundApiClient, ProductCodeLoader productCodeLoader,
                                             StandardTransactionHandler standardTransactionHandler,
                                             RefundTransactionHandler refundTransactionHandler) {
        this.paymentRefundApiClient = paymentRefundApiClient;
        this.productCodeLoader = productCodeLoader;
        this.standardTransactionHandler = standardTransactionHandler;
        this.refundTransactionHandler = refundTransactionHandler;
    }

    public void route(payment_processed paymentReconciliation) {
        PaymentResponse paymentSession = paymentRefundApiClient.getPaymentSession(paymentReconciliation.getPaymentResourceId());
        PaymentDetailsResponse paymentDetails = paymentRefundApiClient.getPaymentDetails(paymentReconciliation.getPaymentResourceId());
        if (isReconcilable(paymentReconciliation)) {
            LOGGER.info("Reconciling payment processed message for payment id: %s".formatted(paymentReconciliation.getPaymentResourceId()));
            PaymentUtils.maskSensitiveFields(paymentSession, productCodeLoader);
            if (isRefundTransaction(paymentReconciliation)) {
                LOGGER.info("Handling refund transaction for payment id: %s".formatted(paymentReconciliation.getPaymentResourceId()));
                refundTransactionHandler.handle(paymentSession, paymentReconciliation);
            } else if (paymentDetails.getPaymentStatus() != null && paymentDetails.getPaymentStatus().equals("accepted")) {
                LOGGER.info("Handling standard transaction for payment id: %s".formatted(paymentReconciliation.getPaymentResourceId()));
                standardTransactionHandler.handle(paymentDetails, paymentSession);
            } else {
                LOGGER.info("Payment processed message has unhandled status for payment id: %s".formatted(paymentReconciliation.getPaymentResourceId()));
            }
        } else {
            LOGGER.info("Payment processed message is not reconcilable for payment id: %s".formatted(paymentReconciliation.getPaymentResourceId()));
        }
    }



    private boolean isRefundTransaction(payment_processed paymentReconciliation) {
        return paymentReconciliation.getRefundId() != null && !paymentReconciliation.getRefundId().isEmpty();
    }

    private static final String DATA_MAINTENANCE = "data-maintenance";
    private static final String ORDERABLE_ITEM = "orderable-item";

    private boolean isReconcilable(payment_processed paymentReconciliation) {
        PaymentResponse paymentSession = paymentRefundApiClient.getPaymentSession(paymentReconciliation.getPaymentResourceId());
        String classOfPayment = paymentSession.getCosts().getFirst().getClassOfPayment().get(0);
        String productType = paymentSession.getCosts().getFirst().getProductType();
        Integer productCode = productCodeLoader.getProductCodes().get(productType);

        boolean isClassValid = DATA_MAINTENANCE.equals(classOfPayment) || ORDERABLE_ITEM.equals(classOfPayment);
        boolean isProductCodeValid = productCode != null && productCode != 0;

        return isClassValid && isProductCodeValid;
    }

}
