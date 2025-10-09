package uk.gov.companieshouse.paymentreconciliation.consumer.service;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import payments.payment_processed;
import uk.gov.companieshouse.api.payments.Cost;
import uk.gov.companieshouse.api.payments.PaymentDetailsResponse;
import uk.gov.companieshouse.api.payments.PaymentResponse;
import uk.gov.companieshouse.paymentreconciliation.consumer.apiclient.PaymentsApiClient;
import uk.gov.companieshouse.paymentreconciliation.consumer.config.ProductCodeLoader;
import uk.gov.companieshouse.paymentreconciliation.consumer.service.handler.RefundTransactionHandler;
import uk.gov.companieshouse.paymentreconciliation.consumer.service.handler.StandardTransactionHandler;

class PaymentReconciliationServiceRouterTest {

    private PaymentsApiClient paymentsApiClient;
    private ProductCodeLoader productCodeLoader;
    private StandardTransactionHandler standardTransactionHandler;
    private RefundTransactionHandler refundTransactionHandler;
    private PaymentReconciliationServiceRouter router;

    @BeforeEach
    void setUp() {
        paymentsApiClient = mock(PaymentsApiClient.class);
        productCodeLoader = mock(ProductCodeLoader.class);
        standardTransactionHandler = mock(StandardTransactionHandler.class);
        refundTransactionHandler = mock(RefundTransactionHandler.class);
        router = new PaymentReconciliationServiceRouter(paymentsApiClient, productCodeLoader, standardTransactionHandler, refundTransactionHandler);
    }

    private PaymentResponse buildPaymentResponse(String classOfPayment, String productType) {
        PaymentResponse paymentResponse = mock(PaymentResponse.class);
        Cost cost = mock(Cost.class);
        List<String> classOfPaymentList = Collections.singletonList(classOfPayment);
        when(cost.getClassOfPayment()).thenReturn(classOfPaymentList);
        when(cost.getProductType()).thenReturn(productType);
        List<Cost> costs = Collections.singletonList(cost);
        when(paymentResponse.getCosts()).thenReturn(costs);
        return paymentResponse;
    }

    @Test
    void route_handlesRefundTransaction_whenReconcilableAndRefund() {
        payment_processed paymentreconciliation = mock(payment_processed.class);
        when(paymentreconciliation.getPaymentResourceId()).thenReturn("pid");
        when(paymentreconciliation.getRefundId()).thenReturn("refundId");

        PaymentResponse paymentSession = buildPaymentResponse("data-maintenance", "productA");
        PaymentDetailsResponse paymentDetails = mock(PaymentDetailsResponse.class);

        Map<String, Integer> productCodes = new HashMap<>();
        productCodes.put("productA", 1);

        when(paymentsApiClient.getPaymentSession("pid")).thenReturn(paymentSession);
        when(paymentsApiClient.getPaymentDetails("pid")).thenReturn(paymentDetails);
        when(productCodeLoader.getProductCodes()).thenReturn(productCodes);

        router.route(paymentreconciliation);

        verify(refundTransactionHandler).handle(paymentSession, paymentreconciliation);
        verifyNoInteractions(standardTransactionHandler);
    }

    @Test
    void route_handlesStandardTransaction_whenReconcilableAndAccepted() {
        payment_processed paymentreconciliation = mock(payment_processed.class);
        when(paymentreconciliation.getPaymentResourceId()).thenReturn("pid");
        when(paymentreconciliation.getRefundId()).thenReturn(null);

        PaymentResponse paymentSession = buildPaymentResponse("orderable-item", "productB");
        PaymentDetailsResponse paymentDetails = mock(PaymentDetailsResponse.class);

        Map<String, Integer> productCodes = new HashMap<>();
        productCodes.put("productB", 2);

        when(paymentsApiClient.getPaymentSession("pid")).thenReturn(paymentSession);
        when(paymentsApiClient.getPaymentDetails("pid")).thenReturn(paymentDetails);
        when(productCodeLoader.getProductCodes()).thenReturn(productCodes);
        when(paymentDetails.getPaymentStatus()).thenReturn("accepted");

        router.route(paymentreconciliation);

        verify(standardTransactionHandler).handle(paymentDetails, paymentSession);
        verifyNoInteractions(refundTransactionHandler);
    }

    @Test
    void route_doesNothing_whenNotReconcilable() {
        payment_processed paymentreconciliation = mock(payment_processed.class);
        when(paymentreconciliation.getPaymentResourceId()).thenReturn("pid");
        when(paymentreconciliation.getRefundId()).thenReturn(null);

        PaymentResponse paymentSession = buildPaymentResponse("other-class", "productC");
        PaymentDetailsResponse paymentDetails = mock(PaymentDetailsResponse.class);

        when(paymentsApiClient.getPaymentSession("pid")).thenReturn(paymentSession);
        when(paymentsApiClient.getPaymentDetails("pid")).thenReturn(paymentDetails);

        router.route(paymentreconciliation);

        verifyNoInteractions(standardTransactionHandler, refundTransactionHandler);
    }

    @Test
    void route_doesNothing_whenProductTypeNotInProductCodes() {
        payment_processed paymentreconciliation = mock(payment_processed.class);
        when(paymentreconciliation.getPaymentResourceId()).thenReturn("pid");
        when(paymentreconciliation.getRefundId()).thenReturn(null);

        PaymentResponse paymentSession = buildPaymentResponse("data-maintenance", "unknownProduct");
        PaymentDetailsResponse paymentDetails = mock(PaymentDetailsResponse.class);

        Map<String, Integer> productCodes = new HashMap<>();
        productCodes.put("productA", 1);

        when(paymentsApiClient.getPaymentSession("pid")).thenReturn(paymentSession);
        when(paymentsApiClient.getPaymentDetails("pid")).thenReturn(paymentDetails);
        when(productCodeLoader.getProductCodes()).thenReturn(productCodes);

        router.route(paymentreconciliation);

        verifyNoInteractions(standardTransactionHandler, refundTransactionHandler);
    }

    @Test
    void route_doesNothing_whenProductCodeIsZero() {
        payment_processed paymentreconciliation = mock(payment_processed.class);
        when(paymentreconciliation.getPaymentResourceId()).thenReturn("pid");
        when(paymentreconciliation.getRefundId()).thenReturn(null);

        PaymentResponse paymentSession = buildPaymentResponse("orderable-item", "productZero");
        PaymentDetailsResponse paymentDetails = mock(PaymentDetailsResponse.class);

        Map<String, Integer> productCodes = new HashMap<>();
        productCodes.put("productZero", 0);

        when(paymentsApiClient.getPaymentSession("pid")).thenReturn(paymentSession);
        when(paymentsApiClient.getPaymentDetails("pid")).thenReturn(paymentDetails);
        when(productCodeLoader.getProductCodes()).thenReturn(productCodes);

        router.route(paymentreconciliation);

        verifyNoInteractions(standardTransactionHandler, refundTransactionHandler);
    }

    @Test
    void route_doesNothing_whenPaymentStatusIsNotAccepted() {
        payment_processed paymentreconciliation = mock(payment_processed.class);
        when(paymentreconciliation.getPaymentResourceId()).thenReturn("pid");
        when(paymentreconciliation.getRefundId()).thenReturn(null);

        PaymentResponse paymentSession = buildPaymentResponse("orderable-item", "productB");
        PaymentDetailsResponse paymentDetails = mock(PaymentDetailsResponse.class);

        Map<String, Integer> productCodes = new HashMap<>();
        productCodes.put("productB", 2);

        when(paymentsApiClient.getPaymentSession("pid")).thenReturn(paymentSession);
        when(paymentsApiClient.getPaymentDetails("pid")).thenReturn(paymentDetails);
        when(productCodeLoader.getProductCodes()).thenReturn(productCodes);
        when(paymentDetails.getPaymentStatus()).thenReturn("pending");

        router.route(paymentreconciliation);

        verifyNoInteractions(standardTransactionHandler, refundTransactionHandler);
    }
}