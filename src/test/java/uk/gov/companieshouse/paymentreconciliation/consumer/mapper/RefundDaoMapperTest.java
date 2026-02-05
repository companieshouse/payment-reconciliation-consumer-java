package uk.gov.companieshouse.paymentreconciliation.consumer.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import uk.gov.companieshouse.api.model.payment.PaymentResponse;
import uk.gov.companieshouse.api.model.payment.RefundModel;
import uk.gov.companieshouse.api.payments.Cost;
import uk.gov.companieshouse.api.payments.CreatedBy;
import uk.gov.companieshouse.paymentreconciliation.consumer.config.ProductCodeLoader;
import uk.gov.companieshouse.paymentreconciliation.consumer.model.RefundDao;

class RefundDaoMapperTest {

    private ProductCodeLoader productCodeLoader;
    private RefundDaoMapper mapper;

    @BeforeEach
    void setUp() {
        productCodeLoader = mock(ProductCodeLoader.class);
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
        int amount = 500;

        LocalDateTime createdAt = LocalDateTime.of(2024, 6, 1, 12, 0);
        LocalDateTime refundedAt = LocalDateTime.of(2024, 6, 2, 13, 0);

        RefundModel refund = mock(RefundModel.class);
        when(refund.getRefundId()).thenReturn(refundId);
        when(refund.getCreatedAt()).thenReturn(Date.from(createdAt.atZone(ZoneId.of("UTC")).toInstant()));
        when(refund.getRefundedAt()).thenReturn(Date.from(refundedAt.atZone(ZoneId.of("UTC")).toInstant()));
        when(refund.getAmount()).thenReturn(amount);
        when(refund.getStatus()).thenReturn(status);

        CreatedBy createdBy = mock(CreatedBy.class);
        when(createdBy.getEmail()).thenReturn(email);

        Cost cost = mock(Cost.class);
        when(cost.getProductType()).thenReturn(productType);

        PaymentResponse paymentSession = mock(PaymentResponse.class);
        when(paymentSession.getCreatedBy()).thenReturn(createdBy);
        when(paymentSession.getPaymentMethod()).thenReturn(paymentMethod);
        when(paymentSession.getCompanyNumber()).thenReturn(companyNumber);
        when(paymentSession.getReference()).thenReturn(reference);
        when(paymentSession.getCosts()).thenReturn(Collections.singletonList(cost));

        // Act
        RefundDao result = mapper.mapFromRefund(paymentId, paymentSession, refund);

        // Assert
        assertEquals("x" + refundId, result.getTransactionId());
        assertEquals("2024-06-01T12:00:00.000+00:00", result.getTransactionDate());
        assertEquals(refundId, result.getRefundId());
        assertEquals("2024-06-02T13:00:00.000+00:00", result.getRefundedAt());
        assertEquals(paymentId, result.getPaymentId());
        assertEquals(email, result.getEmail());
        assertEquals(paymentMethod, result.getPaymentMethod());
        assertEquals(String.valueOf(amount / 100), result.getAmount());
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
        RefundModel refund = mock(RefundModel.class);
        when(refund.getRefundId()).thenReturn("REF456");
        when(refund.getCreatedAt()).thenReturn(Date.from(LocalDateTime.now().atOffset(ZoneId.systemDefault().getRules().getOffset(LocalDateTime.now())).toInstant()));
        when(refund.getRefundedAt()).thenReturn(Date.from(LocalDateTime.now().atOffset(ZoneId.systemDefault().getRules().getOffset(LocalDateTime.now())).toInstant()));
        when(refund.getAmount()).thenReturn(100);
        when(refund.getStatus()).thenReturn("pending");

        CreatedBy createdBy = mock(CreatedBy.class);
        when(createdBy.getEmail()).thenReturn("test@ch.gov.uk");

        Cost cost = mock(Cost.class);
        when(cost.getProductType()).thenReturn("unknownType");

        PaymentResponse paymentSession = mock(PaymentResponse.class);
        when(paymentSession.getCreatedBy()).thenReturn(createdBy);
        when(paymentSession.getPaymentMethod()).thenReturn("card");
        when(paymentSession.getCompanyNumber()).thenReturn("12345678");
        when(paymentSession.getReference()).thenReturn("ORD789");
        when(paymentSession.getCosts()).thenReturn(Collections.singletonList(cost));

        // Act
        RefundDao result = mapper.mapFromRefund(paymentId, paymentSession, refund);

        // Assert
        assertNull(result.getProductCode());
    }
}