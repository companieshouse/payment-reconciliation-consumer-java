package uk.gov.companieshouse.paymentreconciliation.consumer.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.time.Instant;
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


    private void setupPaymentResponseMocks(String email, String paymentMethod, String companyNumber, String reference, String amount1, String amount2) {
        when(paymentResponse.getCosts()).thenReturn(List.of(cost1, cost2));
        when(paymentResponse.getCreatedBy()).thenReturn(createdBy);
        when(createdBy.getEmail()).thenReturn(email);
        when(paymentResponse.getPaymentMethod()).thenReturn(paymentMethod);
        when(cost1.getAmount()).thenReturn(amount1);
        when(cost2.getAmount()).thenReturn(amount2);
        when(paymentResponse.getCompanyNumber()).thenReturn(companyNumber);
        when(paymentResponse.getReference()).thenReturn(reference);
    }

    private void assertPaymentTransactionsResourceDao(PaymentTransactionsResourceDao dao, String paymentId, Instant transactionDate, String email, String paymentMethod, String amount, String companyNumber, String transactionType, String orderReference, String status) {
        assertEquals("X" + paymentId, dao.getTransactionId());
        assertEquals(transactionDate, dao.getTransactionDate());
        assertEquals(email, dao.getEmail());
        assertEquals(paymentMethod, dao.getPaymentMethod());
        assertEquals(amount, dao.getAmount());
        assertEquals(companyNumber, dao.getCompanyNumber());
        assertEquals(transactionType, dao.getTransactionType());
        assertEquals(orderReference, dao.getOrderReference());
        assertEquals(status, dao.getStatus());
        assertEquals("system", dao.getUserId());
        assertEquals("", dao.getOriginalReference());
        assertEquals("", dao.getDisputeDetails());
    }

    @Test
    void mapFromPaymentResponse_shouldMapAllFieldsCorrectly_companyNumberPresent() {
        String paymentId = "PAYID";
        Instant transactionDate = Instant.parse("2024-01-01T12:01:33Z");
        String paymentStatus = "PAID";
        String email = "test@example.com";
        String paymentMethod = "credit-card";
        String companyNumber = "12345678";
        String reference = "REF_001";
        String amount1 = "100";
        String amount2 = "200";

        setupPaymentResponseMocks(email, paymentMethod, companyNumber, reference, amount1, amount2);

        PaymentTransactionsResourceDaoMapper mapper = new PaymentTransactionsResourceDaoMapper();
        List<PaymentTransactionsResourceDao> result = mapper.mapFromPaymentResponse(
                paymentResponse, paymentId, transactionDate, paymentStatus);

        assertEquals(2, result.size());
        assertPaymentTransactionsResourceDao(result.get(0), paymentId, transactionDate, email, paymentMethod, amount1, companyNumber, "Immediate bill", "REF-001", paymentStatus);
        assertEquals(amount2, result.get(1).getAmount());
    }


    @Test
    void mapFromPaymentResponse_shouldMapAllFieldsCorrectly_companyNumberNotPresent() {
        String paymentId = "PAYID";
        Instant transactionDate = Instant.parse("2024-01-01T12:01:33Z");
        String paymentStatus = "PAID";
        String email = "test@example.com";
        String paymentMethod = "credit-card";
        String companyNumber = ""; // Should be empty string if null
        String reference = "REF_001";
        String amount1 = "100";
        String amount2 = "200";

        setupPaymentResponseMocks(email, paymentMethod, null, reference, amount1, amount2);

        PaymentTransactionsResourceDaoMapper mapper = new PaymentTransactionsResourceDaoMapper();
        List<PaymentTransactionsResourceDao> result = mapper.mapFromPaymentResponse(
                paymentResponse, paymentId, transactionDate, paymentStatus);

        assertEquals(2, result.size());
        assertPaymentTransactionsResourceDao(result.get(0), paymentId, transactionDate, email, paymentMethod, amount1, companyNumber, "Immediate bill", "REF-001", paymentStatus);
        assertEquals(amount2, result.get(1).getAmount());
    }

    @Test
    void mapFromPaymentResponse_shouldReturnEmptyListWhenNoCosts() {
        when(paymentResponse.getCosts()).thenReturn(new ArrayList<>());

        PaymentTransactionsResourceDaoMapper mapper = new PaymentTransactionsResourceDaoMapper();
        List<PaymentTransactionsResourceDao> result = mapper.mapFromPaymentResponse(
                paymentResponse, "id", Instant.now(), "status");

        assertTrue(result.isEmpty());
    }
}