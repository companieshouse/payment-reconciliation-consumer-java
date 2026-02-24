package uk.gov.companieshouse.paymentreconciliation.consumer.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.gov.companieshouse.api.model.payment.PaymentResponse;
import uk.gov.companieshouse.api.payments.Cost;
import uk.gov.companieshouse.api.payments.CreatedBy;
import uk.gov.companieshouse.paymentreconciliation.consumer.config.ProductCodeLoader;
import uk.gov.companieshouse.paymentreconciliation.consumer.model.PaymentTransactionsResourceDao;

@ExtendWith(MockitoExtension.class)
class PaymentTransactionsResourceDaoMapperTest {

    @Mock
    private ProductCodeLoader productCodeLoader;
    @Mock
    private PaymentResponse paymentResponse;
    @Mock
    private Cost cost1;
    @Mock
    private Cost cost2;
    @Mock
    private CreatedBy createdBy;

    @Test
    void mapFromPaymentResponse_shouldMapAllFieldsCorrectly() {
        when(paymentResponse.getCosts()).thenReturn(List.of(cost1, cost2));
        when(paymentResponse.getCreatedBy()).thenReturn(createdBy);
        when(createdBy.getEmail()).thenReturn("test@example.com");
        when(paymentResponse.getPaymentMethod()).thenReturn("credit-card");
        when(cost1.getAmount()).thenReturn("100");
        when(cost2.getAmount()).thenReturn("200");
        when(paymentResponse.getCompanyNumber()).thenReturn("12345678");
        when(paymentResponse.getReference()).thenReturn("REF_001");

        String paymentId = "PAYID";
        String transactionDate = "2024-06-01";
        String paymentStatus = "PAID";

        PaymentTransactionsResourceDaoMapper mapper = new PaymentTransactionsResourceDaoMapper();

        // Act
        List<PaymentTransactionsResourceDao> result = mapper.mapFromPaymentResponse(
                paymentResponse, paymentId, transactionDate, paymentStatus);

        // Assert
        assertEquals(2, result.size());

        PaymentTransactionsResourceDao first = result.get(0);
        assertEquals("XPAYID", first.getTransactionId());
        assertEquals(transactionDate, first.getTransactionDate());
        assertEquals("test@example.com", first.getEmail());
        assertEquals("credit-card", first.getPaymentMethod());
        assertEquals("100", first.getAmount());
        assertEquals("12345678", first.getCompanyNumber());
        assertEquals("Immediate bill", first.getTransactionType());
        assertEquals("REF-001", first.getOrderReference());
        assertEquals(paymentStatus, first.getStatus());
        assertEquals("system", first.getUserId());
        assertEquals("", first.getOriginalReference());
        assertEquals("", first.getDisputeDetails());

        PaymentTransactionsResourceDao second = result.get(1);
        assertEquals("200", second.getAmount());
    }

    @Test
    void mapFromPaymentResponse_shouldReturnEmptyListWhenNoCosts() {
        when(paymentResponse.getCosts()).thenReturn(new ArrayList<>());

        PaymentTransactionsResourceDaoMapper mapper = new PaymentTransactionsResourceDaoMapper();
        List<PaymentTransactionsResourceDao> result = mapper.mapFromPaymentResponse(
                paymentResponse, "id", "date", "status");

        assertTrue(result.isEmpty());
    }
}