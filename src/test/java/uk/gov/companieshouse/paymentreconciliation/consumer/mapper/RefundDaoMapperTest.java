package uk.gov.companieshouse.paymentreconciliation.consumer.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.gov.companieshouse.api.model.payment.PaymentResponse;
import uk.gov.companieshouse.api.model.payment.RefundModel;
import uk.gov.companieshouse.api.payments.Cost;
import uk.gov.companieshouse.api.payments.CreatedBy;
import uk.gov.companieshouse.paymentreconciliation.consumer.config.ProductCodeLoader;
import uk.gov.companieshouse.paymentreconciliation.consumer.model.RefundDao;

@ExtendWith(MockitoExtension.class)

class RefundDaoMapperTest {

    @Mock
    private ProductCodeLoader productCodeLoader;
    @Mock
    private PaymentResponse paymentResponse;
    @Mock
    private Cost cost;
    @Mock
    private CreatedBy createdBy;
    @Mock
    private RefundModel refund;

    private RefundDaoMapper mapper;

    @BeforeEach
    void setUp() {
        Map<String, Integer> productCodes = new HashMap<>();
        productCodes.put("productTypeA", 1234);
        when(productCodeLoader.getProductCodes()).thenReturn(productCodes);
        mapper = new RefundDaoMapper(productCodeLoader);
    }


    private void setupMocks(String refundId, String email, String paymentMethod, String companyNumber,
                           String reference, String status, String productType, int amount,
                           Instant createdAt, Instant refundedAt) {
        when(refund.getRefundId()).thenReturn(refundId);
        when(refund.getCreatedAt()).thenReturn(createdAt);
        when(refund.getRefundedAt()).thenReturn(refundedAt);
        when(refund.getAmount()).thenReturn(amount);
        when(refund.getStatus()).thenReturn(status);

        when(createdBy.getEmail()).thenReturn(email);

        when(cost.getProductType()).thenReturn(productType);

        when(paymentResponse.getCreatedBy()).thenReturn(createdBy);
        when(paymentResponse.getPaymentMethod()).thenReturn(paymentMethod);
        when(paymentResponse.getCompanyNumber()).thenReturn(companyNumber);
        when(paymentResponse.getReference()).thenReturn(reference);
        when(paymentResponse.getCosts()).thenReturn(Collections.singletonList(cost));
    }

    private void assertRefundDaoFields(RefundDao result, String paymentId, String refundId, String email, String paymentMethod,
                                       String companyNumber, String reference, String status, String amount,
                                       Instant createdAt, Instant refundedAt, Integer productCode) {
        assertEquals("x" + refundId, result.getTransactionId());
        assertEquals(createdAt, result.getTransactionDate());
        assertEquals(refundId, result.getRefundId());
        assertEquals(refundedAt, result.getRefundedAt());
        assertEquals(paymentId, result.getPaymentId());
        assertEquals(email, result.getEmail());
        assertEquals(paymentMethod, result.getPaymentMethod());
        assertEquals(amount, result.getAmount());
        assertEquals(companyNumber, result.getCompanyNumber());
        assertEquals("refund", result.getTransactionType());
        assertEquals(reference, result.getOrderReference());
        assertEquals(status, result.getStatus());
        assertEquals("system", result.getUserId());
        assertEquals("X" + paymentId, result.getOriginalReference());
        assertEquals("", result.getDisputeDetails());
        if (productCode == null) {
            assertNull(result.getProductCode());
        } else {
            assertEquals(productCode.intValue(), result.getProductCode());
        }
    }

    @Test
    void mapFromRefund_mapsAllFieldsCorrectly_companyNumberPresent() {
        String paymentId = "PAY123";
        String refundId = "REF456";
        String email = "test@ch.gov.uk";
        String paymentMethod = "card";
        String companyNumber = "12345678";
        String reference = "ORD789";
        String status = "pending";
        String productType = "productTypeA";
        int amount = 50000;
        Instant createdAt = Instant.parse("2024-01-01T12:01:33Z");
        Instant refundedAt = Instant.parse("2024-01-02T11:55:12Z");

        setupMocks(refundId, email, paymentMethod, companyNumber, reference, status, productType, amount, createdAt, refundedAt);

        RefundDao result = mapper.mapFromRefund(paymentId, paymentResponse, refund);
        assertRefundDaoFields(result, paymentId, refundId, email, paymentMethod, companyNumber, reference, status, "500", createdAt, refundedAt, 1234);
    }


    @Test
    void mapFromRefund_mapsAllFieldsCorrectly_companyNumberNotPresent() {
        String paymentId = "PAY123";
        String refundId = "REF456";
        String email = "test@ch.gov.uk";
        String paymentMethod = "card";
        String companyNumber = ""; // Should be empty string if null
        String reference = "ORD789";
        String status = "pending";
        String productType = "productTypeA";
        int amount = 50000;
        Instant createdAt = Instant.parse("2024-01-01T12:01:33Z");
        Instant refundedAt = Instant.parse("2024-01-02T11:55:12Z");

        setupMocks(refundId, email, paymentMethod, null, reference, status, productType, amount, createdAt, refundedAt);

        RefundDao result = mapper.mapFromRefund(paymentId, paymentResponse, refund);
        assertRefundDaoFields(result, paymentId, refundId, email, paymentMethod, companyNumber, reference, status, "500", createdAt, refundedAt, 1234);
    }



    @Test
    void mapFromRefund_nullProductType_returnsNullProductCode() {
        String paymentId = "PAY123";
        String refundId = "REF456";
        String email = "test@ch.gov.uk";
        String paymentMethod = "card";
        String companyNumber = "12345678";
        String reference = "ORD789";
        String status = "pending";
        String productType = "unknownType";
        int amount = 100;
        Instant createdAt = Instant.now();
        Instant refundedAt = Instant.now();

        setupMocks(refundId, email, paymentMethod, companyNumber, reference, status, productType, amount, createdAt, refundedAt);

        RefundDao result = mapper.mapFromRefund(paymentId, paymentResponse, refund);
        assertRefundDaoFields(result, paymentId, refundId, email, paymentMethod, companyNumber, reference, status, "1", createdAt, refundedAt, null);
    }
}