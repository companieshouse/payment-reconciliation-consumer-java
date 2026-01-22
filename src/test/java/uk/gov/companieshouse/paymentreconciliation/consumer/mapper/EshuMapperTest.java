
package uk.gov.companieshouse.paymentreconciliation.consumer.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import uk.gov.companieshouse.api.payments.Cost;
import uk.gov.companieshouse.api.model.payment.PaymentResponse;
import uk.gov.companieshouse.paymentreconciliation.consumer.config.ProductCodeLoader;
import uk.gov.companieshouse.paymentreconciliation.consumer.model.EshuDao;

class EshuMapperTest {

    private ProductCodeLoader productCodeLoader;
    private EshuMapper eshuMapper;

    @BeforeEach
    void setUp() {
        productCodeLoader = mock(ProductCodeLoader.class);
        Map<String, Integer> productCodes = new HashMap<>();
        productCodes.put("typeA", 100);
        productCodes.put("typeB", 200);
        when(productCodeLoader.getProductCodes()).thenReturn(productCodes);
        eshuMapper = new EshuMapper(productCodeLoader);
    }

    @Test
    void mapFromPaymentResponse_mapsSingleCostCorrectly() {
        PaymentResponse paymentResponse = mock(PaymentResponse.class);
        Cost cost = mock(Cost.class);

        when(cost.getProductType()).thenReturn("typeA");
        when(paymentResponse.getCosts()).thenReturn(List.of(cost));
        when(paymentResponse.getCompanyNumber()).thenReturn("12345678");

        String paymentId = "PAYID";
        String transactionDate = "2024-06-01";

        List<EshuDao> result = eshuMapper.mapFromPaymentResponse(paymentResponse, paymentId, transactionDate);

        assertEquals(1, result.size());
        EshuDao eshu = result.get(0);
        assertEquals("X" + paymentId, eshu.getPaymentRef());
        assertEquals(100, eshu.getProductCode());
        assertEquals("12345678", eshu.getCompanyNumber());
        assertEquals("", eshu.getFilingDate());
        assertEquals("", eshu.getMadeUpdate());
        assertEquals(transactionDate, eshu.getTransactionDate());
    }

    @Test
    void mapFromPaymentResponse_mapsMultipleCostsCorrectly() {
        PaymentResponse paymentResponse = mock(PaymentResponse.class);
        Cost cost1 = mock(Cost.class);
        Cost cost2 = mock(Cost.class);

        when(cost1.getProductType()).thenReturn("typeA");
        when(cost2.getProductType()).thenReturn("typeB");
        when(paymentResponse.getCosts()).thenReturn(List.of(cost1, cost2));
        when(paymentResponse.getCompanyNumber()).thenReturn("87654321");

        String paymentId = "MULTI";
        String transactionDate = "2024-06-02";

        List<EshuDao> result = eshuMapper.mapFromPaymentResponse(paymentResponse, paymentId, transactionDate);

        assertEquals(2, result.size());

        EshuDao eshu1 = result.get(0);
        assertEquals("X" + paymentId, eshu1.getPaymentRef());
        assertEquals(100, eshu1.getProductCode());
        assertEquals("87654321", eshu1.getCompanyNumber());
        assertEquals(transactionDate, eshu1.getTransactionDate());

        EshuDao eshu2 = result.get(1);
        assertEquals("X" + paymentId, eshu2.getPaymentRef());
        assertEquals(200, eshu2.getProductCode());
        assertEquals("87654321", eshu2.getCompanyNumber());
        assertEquals(transactionDate, eshu2.getTransactionDate());
    }

    @Test
    void mapFromPaymentResponse_returnsEmptyListWhenNoCosts() {
        PaymentResponse paymentResponse = mock(PaymentResponse.class);
        when(paymentResponse.getCosts()).thenReturn(Collections.emptyList());

        List<EshuDao> result = eshuMapper.mapFromPaymentResponse(paymentResponse, "ID", "2024-06-03");

        assertTrue(result.isEmpty());
    }
}