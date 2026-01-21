package uk.gov.companieshouse.paymentreconciliation.consumer.mapper;

import uk.gov.companieshouse.api.model.payment.PaymentResponse;
import uk.gov.companieshouse.paymentreconciliation.consumer.model.PaymentTransactionsResourceDao;
import java.util.List;
import java.util.ArrayList;

import org.springframework.stereotype.Component;

@Component
public class PaymentTransactionsResourceDaoMapper {
    public List<PaymentTransactionsResourceDao> mapFromPaymentResponse(PaymentResponse paymentResponse, String paymentId, String transactionDate, String paymentStatus) {
        List<PaymentTransactionsResourceDao> paymentTransactionsResources = new ArrayList<>();
        for (var cost : paymentResponse.getCosts()) {
            PaymentTransactionsResourceDao transaction = new PaymentTransactionsResourceDao();
            transaction.setTransactionId("X" + paymentId);
            transaction.setTransactionDate(transactionDate);
            transaction.setEmail(paymentResponse.getCreatedBy().getEmail());
            transaction.setPaymentMethod(paymentResponse.getPaymentMethod());
            transaction.setAmount(String.valueOf(cost.getAmount()));
            transaction.setCompanyNumber(paymentResponse.getCompanyNumber());
            transaction.setTransactionType("Immediate bill");
            transaction.setOrderReference(paymentResponse.getReference().replace("_", "-"));
            transaction.setStatus(paymentStatus);
            transaction.setUserId("system");
            transaction.setOriginalReference("");
            transaction.setDisputeDetails("");
            paymentTransactionsResources.add(transaction);
        }
        return paymentTransactionsResources;
    }
}
