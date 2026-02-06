package uk.gov.companieshouse.paymentreconciliation.consumer.mapper;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Component;

import uk.gov.companieshouse.api.model.payment.PaymentResponse;
import uk.gov.companieshouse.api.model.payment.RefundModel;
import uk.gov.companieshouse.paymentreconciliation.consumer.config.ProductCodeLoader;
import uk.gov.companieshouse.paymentreconciliation.consumer.model.RefundDao;

@Component
public class RefundDaoMapper {
    
    private final ProductCodeLoader productCodeLoader;
    
    private static final int PENCE_IN_POUND = 100;
    
    private static final DateTimeFormatter REFUND_DATE_FORMATTER  = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSxxx")
                                       .withZone(ZoneOffset.UTC);
    public RefundDaoMapper(ProductCodeLoader productCodeLoader) {
        this.productCodeLoader = productCodeLoader;
    }



    public RefundDao mapFromRefund(String paymentId, PaymentResponse paymentSession, RefundModel refund) {
        RefundDao refundDao = new RefundDao();
        refundDao.setTransactionId("x" + refund.getRefundId());
        refundDao.setTransactionDate(REFUND_DATE_FORMATTER.format(refund.getCreatedAt().toInstant()));
        refundDao.setRefundId(refund.getRefundId());
        refundDao.setRefundedAt(REFUND_DATE_FORMATTER.format(refund.getRefundedAt().toInstant()));
        refundDao.setPaymentId(paymentId);
        refundDao.setEmail(paymentSession.getCreatedBy().getEmail());
        refundDao.setPaymentMethod(paymentSession.getPaymentMethod());
        // Convert pence to pounds and store as string, this is to preserve trailing zeros for amounts like 100.00        
        refundDao.setAmount(String.valueOf(refund.getAmount() / PENCE_IN_POUND)); 
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
