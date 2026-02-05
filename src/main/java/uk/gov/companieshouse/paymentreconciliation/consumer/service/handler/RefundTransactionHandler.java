package uk.gov.companieshouse.paymentreconciliation.consumer.service.handler;

import static uk.gov.companieshouse.paymentreconciliation.consumer.Application.NAMESPACE;

import org.springframework.retry.RetryException;
import org.springframework.stereotype.Component;

import payments.payment_processed;
import uk.gov.companieshouse.api.model.payment.PaymentResponse;
import uk.gov.companieshouse.api.model.payment.RefundModel;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.paymentreconciliation.consumer.apiclient.PaymentsApiClient;
import uk.gov.companieshouse.paymentreconciliation.consumer.mapper.RefundDaoMapper;
import uk.gov.companieshouse.paymentreconciliation.consumer.model.RefundDao;
import uk.gov.companieshouse.paymentreconciliation.consumer.repository.RefundRepository;

@Component
public class RefundTransactionHandler implements TransactionHandler<PaymentResponse, payment_processed> {
    private final PaymentsApiClient paymentRefundApiClient;
    private final RefundRepository refundRepository;
    private final RefundDaoMapper refundDaoMapper;
    private static final Logger LOGGER = LoggerFactory.getLogger(NAMESPACE);

    private static final String STATUS_SUBMITTED = "submitted";
    private static final String STATUS_REFUND_REQUESTED = "refund-requested";
    private static final String STATUS_SUCCESS = "success";
    private static final String STATUS_REFUND_SUCCESS = "refund-success";
    private static final String STATUS_FAILED = "failed";

    public RefundTransactionHandler(PaymentsApiClient paymentRefundApiClient, RefundRepository refundRepository, RefundDaoMapper refundDaoMapper) {
        this.paymentRefundApiClient = paymentRefundApiClient;
        this.refundRepository = refundRepository;
        this.refundDaoMapper = refundDaoMapper;
    }

    @Override
    public void handle(PaymentResponse paymentSession, payment_processed paymentProcessed) {

        RefundModel refund = getRefund(paymentSession, paymentProcessed);
        if (refund != null) {
            if (refund.getStatus().equals(STATUS_SUBMITTED) || refund.getStatus().equals(STATUS_REFUND_REQUESTED)) {
                LOGGER.info("Refund status is submitted. Fetching latest refund status: " + refund);
                refund = paymentRefundApiClient.patchLatestRefundStatus(paymentProcessed.getPaymentResourceId(), refund);
            }
            if (refund != null) {
                if (refund.getStatus().equals(STATUS_SUCCESS) || refund.getStatus().equals(STATUS_REFUND_SUCCESS)) {
                    LOGGER.info("Refund successful. Reconciling...: %s".formatted(refund));
                    reconcileRefund(paymentProcessed.getPaymentResourceId(), paymentSession, refund);
                } else if (refund.getStatus().equals(STATUS_FAILED)) {
                    LOGGER.info("Refund failed. Skipping reconciliation: %s".formatted(refund));
                } else {
                    LOGGER.info("Refund status is still submitted, retrying: %s".formatted(refund));
                    throw new RetryException("Refund status is still submitted");
                }
            } else {
                LOGGER.debug("Refund is null after attempting to fetch latest status. Skipping further processing.");
            }
        }
    }

    private RefundModel getRefund(PaymentResponse paymentSession, payments.payment_processed paymentReconciliation) {
        RefundModel matchedRefund = null;
        if (paymentReconciliation.getRefundId() != null && !paymentReconciliation.getRefundId().isEmpty()) {
            if (paymentSession.getRefunds() == null) {
                return null;
            }

            for (var refund : paymentSession.getRefunds()) {
                if (refund.getRefundId().equals(paymentReconciliation.getRefundId())) {
                    matchedRefund = refund;
                    break;
                }
            }
        }
        return matchedRefund;
    }

    private void reconcileRefund(String paymentId, PaymentResponse paymentSession, RefundModel refund) {
        LOGGER.info("Creating refund record for payment id: %s".formatted(paymentId));
        RefundDao refundDao = refundDaoMapper.mapFromRefund(paymentId, paymentSession, refund);
        refundRepository.save(refundDao);
    }
}
