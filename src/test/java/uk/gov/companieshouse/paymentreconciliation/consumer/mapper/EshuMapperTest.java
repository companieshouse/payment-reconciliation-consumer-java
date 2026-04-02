
package uk.gov.companieshouse.paymentreconciliation.consumer.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.payments.Cost;
import uk.gov.companieshouse.api.model.payment.PaymentResponse;
import uk.gov.companieshouse.paymentreconciliation.consumer.config.ProductCodeLoader;
import uk.gov.companieshouse.paymentreconciliation.consumer.model.EshuDao;

@ExtendWith(MockitoExtension.class)
class EshuMapperTest {

    @Mock
    private ProductCodeLoader productCodeLoader;
    @Mock
    private PaymentResponse paymentResponse;
    @Mock
    private Cost cost1;
    @Mock
    private Cost cost2;


    private EshuMapper eshuMapper;

    @BeforeEach
    void setUp() {
        eshuMapper = new EshuMapper(productCodeLoader);
    }


    private void setupProductCodes(Map<String, Integer> productCodes) {
        when(productCodeLoader.getProductCodes()).thenReturn(productCodes);
    }

    private void setupPaymentResponseCosts(List<Cost> costs, String companyNumber) {
        when(paymentResponse.getCosts()).thenReturn(costs);
        when(paymentResponse.getCompanyNumber()).thenReturn(companyNumber);
    }

    private void assertEshuDao(EshuDao eshu, String paymentId, int productCode, String companyNumber, Instant transactionDate) {
        assertEquals("X" + paymentId, eshu.getPaymentReference());
        assertEquals(productCode, eshu.getProductCode());
        assertEquals(companyNumber, eshu.getCompanyNumber());
        assertEquals("", eshu.getFilingDate());
        assertEquals("", eshu.getMadeUpDate());
        assertEquals(transactionDate, eshu.getTransactionDate());
    }

    @Test
    void mapFromPaymentResponse_mapsSingleCostCorrectly_companyNumberPresent() {
        Map<String, Integer> productCodes = new HashMap<>();
        productCodes.put("typeA", 100);
        productCodes.put("typeB", 200);
        setupProductCodes(productCodes);

        when(cost1.getProductType()).thenReturn("typeA");
        setupPaymentResponseCosts(List.of(cost1), "12345678");

        String paymentId = "PAYID";
        Instant transactionDate = Instant.parse("2024-01-01T12:01:33Z");

        List<EshuDao> result = eshuMapper.mapFromPaymentResponse(paymentResponse, paymentId, transactionDate);

        assertEquals(1, result.size());
        assertEshuDao(result.get(0), paymentId, 100, "12345678", transactionDate);
    }


    @Test
    void mapFromPaymentResponse_mapsSingleCostCorrectly_companyNumberNotPresent() {
        Map<String, Integer> productCodes = new HashMap<>();
        productCodes.put("typeA", 100);
        productCodes.put("typeB", 200);
        setupProductCodes(productCodes);

        when(cost1.getProductType()).thenReturn("typeA");
        setupPaymentResponseCosts(List.of(cost1), null);

        String paymentId = "PAYID";
        Instant transactionDate = Instant.parse("2024-01-01T12:01:33Z");

        List<EshuDao> result = eshuMapper.mapFromPaymentResponse(paymentResponse, paymentId, transactionDate);

        assertEquals(1, result.size());
        assertEshuDao(result.get(0), paymentId, 100, "", transactionDate);
    }


    @Test
    void mapFromPaymentResponse_mapsMultipleCostsCorrectly() {
        Map<String, Integer> productCodes = new HashMap<>();
        productCodes.put("typeA", 100);
        productCodes.put("typeB", 200);
        setupProductCodes(productCodes);

        when(cost1.getProductType()).thenReturn("typeA");
        when(cost2.getProductType()).thenReturn("typeB");
        setupPaymentResponseCosts(List.of(cost1, cost2), "87654321");

        String paymentId = "MULTI";
        Instant transactionDate = Instant.parse("2024-01-01T12:01:33Z");

        List<EshuDao> result = eshuMapper.mapFromPaymentResponse(paymentResponse, paymentId, transactionDate);

        assertEquals(2, result.size());
        assertEshuDao(result.get(0), paymentId, 100, "87654321", transactionDate);
        assertEshuDao(result.get(1), paymentId, 200, "87654321", transactionDate);
    }

    @Test
    void mapFromPaymentResponse_returnsEmptyListWhenNoCosts() {
        when(paymentResponse.getCosts()).thenReturn(Collections.emptyList());
        Instant transactionDate = Instant.parse("2024-01-01T12:01:33Z");

        List<EshuDao> result = eshuMapper.mapFromPaymentResponse(paymentResponse, "ID", transactionDate);

        assertTrue(result.isEmpty());
    }
}