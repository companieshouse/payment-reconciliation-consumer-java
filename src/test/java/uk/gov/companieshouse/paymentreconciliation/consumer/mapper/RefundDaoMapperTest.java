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

    @Test
    void mapFromRefund_mapsAllFieldsCorrectly() {

        // Arrange
        String paymentId = "PAY123";
        String refundId = "REF456";
        String email = "test@ch.gov.uk";
        String paymentMethod = "card";
        String companyNumber = "12345678";
        String reference = "ORD789";
        String status = "pending";
        String productType = "productTypeA";
        int amount = 50000;

        Instant createdAt = Instant.parse("2024-01-01T00:00:00Z");
        Instant refundedAt = Instant.parse("2024-01-02T11:55:12Z");

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

        // Act
        RefundDao result = mapper.mapFromRefund(paymentId, paymentResponse, refund);


        // Format to string
        assertEquals("x" + refundId, result.getTransactionId());
        assertEquals("2024-01-01T00:00:00.000+00:00", result.getTransactionDate());
        assertEquals(refundId, result.getRefundId());
        assertEquals("2024-01-02T11:55:12.000+00:00", result.getRefundedAt());
        assertEquals(paymentId, result.getPaymentId());
        assertEquals(email, result.getEmail());
        assertEquals(paymentMethod, result.getPaymentMethod());
        assertEquals("500", result.getAmount());
        assertEquals(companyNumber, result.getCompanyNumber());
        assertEquals("refund", result.getTransactionType());
        assertEquals(reference, result.getOrderReference());
        assertEquals(status, result.getStatus());
        assertEquals("system", result.getUserId());
        assertEquals("X" + paymentId, result.getOriginalReference());
        assertEquals("", result.getDisputeDetails());
        assertEquals(1234, result.getProductCode());
    }

    @Test
    void mapFromRefund_nullProductType_returnsNullProductCode() {
        // Arrange
        String paymentId = "PAY123";
        when(refund.getRefundId()).thenReturn("REF456");
        when(refund.getCreatedAt()).thenReturn(Instant.now());
        when(refund.getRefundedAt()).thenReturn(Instant.now());
        when(refund.getAmount()).thenReturn(100);
        when(refund.getStatus()).thenReturn("pending");

        when(createdBy.getEmail()).thenReturn("test@ch.gov.uk");

        when(cost.getProductType()).thenReturn("unknownType");

        when(paymentResponse.getCreatedBy()).thenReturn(createdBy);
        when(paymentResponse.getPaymentMethod()).thenReturn("card");
        when(paymentResponse.getCompanyNumber()).thenReturn("12345678");
        when(paymentResponse.getReference()).thenReturn("ORD789");
        when(paymentResponse.getCosts()).thenReturn(Collections.singletonList(cost));

        // Act
        RefundDao result = mapper.mapFromRefund(paymentId, paymentResponse, refund);

        // Assert
        assertNull(result.getProductCode());
    }
}