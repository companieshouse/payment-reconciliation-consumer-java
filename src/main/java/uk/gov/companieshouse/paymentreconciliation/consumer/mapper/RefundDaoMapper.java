package uk.gov.companieshouse.paymentreconciliation.consumer.mapper;

import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Component;

import uk.gov.companieshouse.api.payments.PaymentResponse;
import uk.gov.companieshouse.api.payments.Refund;
import uk.gov.companieshouse.paymentreconciliation.consumer.config.ProductCodeLoader;
import uk.gov.companieshouse.paymentreconciliation.consumer.model.RefundDao;

@Component
public class RefundDaoMapper {
    private final ProductCodeLoader productCodeLoader;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public RefundDaoMapper(ProductCodeLoader productCodeLoader) {
        this.productCodeLoader = productCodeLoader;
    }

    public RefundDao mapFromRefund(String paymentId, PaymentResponse paymentSession, Refund refund) {
        RefundDao refundDao = new RefundDao();
        refundDao.setTransactionId("x" + refund.getRefundId());
        refundDao.setTransactionDate(refund.getCreatedAt().format(DATE_TIME_FORMATTER));
        refundDao.setRefundId(refund.getRefundId());
        refundDao.setRefundedAt(refund.getRefundedAt().format(DATE_TIME_FORMATTER));
        refundDao.setPaymentId(paymentId);
        refundDao.setEmail(paymentSession.getCreatedBy().getEmail());
        refundDao.setPaymentMethod(paymentSession.getPaymentMethod());
        refundDao.setAmount(String.valueOf(refund.getAmount()));
        refundDao.setCompanyNumber(paymentSession.getCompanyNumber());
        refundDao.setTransactionType("refund");
        refundDao.setOrderReference(paymentSession.getReference());
        refundDao.setStatus(refund.getStatus());
        refundDao.setUserId("system");
        refundDao.setOriginalReference("X"+paymentId);
        refundDao.setDisputeDetails("");
        String productType = paymentSession.getCosts().getFirst().getProductType();
        refundDao.setProductCode(productCodeLoader.getProductCodes().get(productType));
        return refundDao;
    }
}
