package uk.gov.companieshouse.paymentreconciliation.consumer.service;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import consumer.exception.RetryableErrorException;
import payments.payment_processed;
import uk.gov.companieshouse.api.model.payment.PaymentResponse;
import uk.gov.companieshouse.api.payments.Cost;
import uk.gov.companieshouse.api.payments.PaymentDetailsResponse;
import uk.gov.companieshouse.paymentreconciliation.consumer.apiclient.PaymentsApiClient;
import uk.gov.companieshouse.paymentreconciliation.consumer.config.ProductCodeLoader;
import uk.gov.companieshouse.paymentreconciliation.consumer.service.handler.RefundTransactionHandler;
import uk.gov.companieshouse.paymentreconciliation.consumer.service.handler.StandardTransactionHandler;


@ExtendWith(MockitoExtension.class)
class PaymentReconciliationServiceRouterTest {

    @Mock
    private PaymentsApiClient paymentsApiClient;

    @Mock
    private ProductCodeLoader productCodeLoader;

    @Mock
    private StandardTransactionHandler standardTransactionHandler;

    @Mock
    private RefundTransactionHandler refundTransactionHandler;

    @Mock
    private payment_processed paymentReconciliation;

    @Mock
    private PaymentDetailsResponse paymentDetails;

    private PaymentReconciliationServiceRouter router;

    @BeforeEach
    void setUp() {
        router = new PaymentReconciliationServiceRouter(paymentsApiClient, productCodeLoader, standardTransactionHandler, refundTransactionHandler);
    }

    private Optional<PaymentResponse> buildPaymentResponse(String classOfPayment, String productType) {
        PaymentResponse paymentResponse = new PaymentResponse();
        List<String> classOfPaymentList = Collections.singletonList(classOfPayment);
        Cost cost = new Cost();
        cost.setClassOfPayment(classOfPaymentList);
        cost.setProductType(productType);
        List<Cost> costs = Collections.singletonList(cost);
        paymentResponse.setCosts(costs);
        return Optional.of(paymentResponse);
    }

    @Test
    void route_handlesRefundTransaction_whenReconcilableAndRefund() {
        when(paymentReconciliation.getPaymentResourceId()).thenReturn("PAY123");
        when(paymentReconciliation.getRefundId()).thenReturn("refundId");

        Optional<PaymentResponse> paymentSession = buildPaymentResponse("data-maintenance", "productA");

        Map<String, Integer> productCodes = new HashMap<>();
        productCodes.put("productA", 1);

        when(paymentsApiClient.getPaymentSession("PAY123")).thenReturn(paymentSession);
        when(paymentsApiClient.getPaymentDetails("PAY123")).thenReturn(paymentDetails);
        when(productCodeLoader.getProductCodes()).thenReturn(productCodes);

        router.route(paymentReconciliation);

        verify(refundTransactionHandler).handle(paymentSession.get(), paymentReconciliation);
        verifyNoInteractions(standardTransactionHandler);
    }

    @Test
    void route_handlesStandardTransaction_whenReconcilableAndAccepted() {
        when(paymentReconciliation.getPaymentResourceId()).thenReturn("PAY123");
        when(paymentReconciliation.getRefundId()).thenReturn(null);

        Optional<PaymentResponse> paymentSession = buildPaymentResponse("orderable-item", "productB");

        Map<String, Integer> productCodes = new HashMap<>();
        productCodes.put("productB", 2);

        when(paymentsApiClient.getPaymentSession("PAY123")).thenReturn(paymentSession);
        when(paymentsApiClient.getPaymentDetails("PAY123")).thenReturn(paymentDetails);
        when(productCodeLoader.getProductCodes()).thenReturn(productCodes);
        when(paymentDetails.getPaymentStatus()).thenReturn("accepted");

        router.route(paymentReconciliation);

        verify(standardTransactionHandler).handle(paymentDetails, paymentSession.get(), "PAY123");
        verifyNoInteractions(refundTransactionHandler);
    }

    @Test
    void route_doesNothing_whenNotReconcilable() {
        when(paymentReconciliation.getPaymentResourceId()).thenReturn("PAY123");

        Optional<PaymentResponse> paymentSession = buildPaymentResponse("other-class", "productC");

        when(paymentsApiClient.getPaymentSession("PAY123")).thenReturn(paymentSession);
        when(paymentsApiClient.getPaymentDetails("PAY123")).thenReturn(paymentDetails);

        router.route(paymentReconciliation);

        verifyNoInteractions(standardTransactionHandler, refundTransactionHandler);
    }

    @Test
    void route_doesNothing_whenProductTypeNotInProductCodes() {
        when(paymentReconciliation.getPaymentResourceId()).thenReturn("PAY123");

        Optional<PaymentResponse> paymentSession = buildPaymentResponse("data-maintenance", "unknownProduct");

        Map<String, Integer> productCodes = new HashMap<>();
        productCodes.put("productA", 1);

        when(paymentsApiClient.getPaymentSession("PAY123")).thenReturn(paymentSession);
        when(paymentsApiClient.getPaymentDetails("PAY123")).thenReturn(paymentDetails);
        when(productCodeLoader.getProductCodes()).thenReturn(productCodes);

        router.route(paymentReconciliation);

        verifyNoInteractions(standardTransactionHandler, refundTransactionHandler);
    }

    @Test
    void route_doesNothing_whenProductCodeIsZero() {
        when(paymentReconciliation.getPaymentResourceId()).thenReturn("PAY123");

        Optional<PaymentResponse> paymentSession = buildPaymentResponse("orderable-item", "productZero");

        Map<String, Integer> productCodes = new HashMap<>();
        productCodes.put("productZero", 0);

        when(paymentsApiClient.getPaymentSession("PAY123")).thenReturn(paymentSession);
        when(paymentsApiClient.getPaymentDetails("PAY123")).thenReturn(paymentDetails);
        when(productCodeLoader.getProductCodes()).thenReturn(productCodes);

        router.route(paymentReconciliation);

        verifyNoInteractions(standardTransactionHandler, refundTransactionHandler);
    }

    @Test
    void route_doesNothing_whenPaymentStatusIsNotAccepted() {
        when(paymentReconciliation.getPaymentResourceId()).thenReturn("PAY123");
        when(paymentReconciliation.getRefundId()).thenReturn(null);

        Optional<PaymentResponse> paymentSession = buildPaymentResponse("orderable-item", "productB");

        Map<String, Integer> productCodes = new HashMap<>();
        productCodes.put("productB", 2);

        when(paymentsApiClient.getPaymentSession("PAY123")).thenReturn(paymentSession);
        when(paymentsApiClient.getPaymentDetails("PAY123")).thenReturn(paymentDetails);
        when(productCodeLoader.getProductCodes()).thenReturn(productCodes);
        when(paymentDetails.getPaymentStatus()).thenReturn("pending");

        router.route(paymentReconciliation);

        verifyNoInteractions(standardTransactionHandler, refundTransactionHandler);
    }

    @Test
    void route_throwsRetryableError_whenPaymentDetailsIsNull() {
        when(paymentReconciliation.getPaymentResourceId()).thenReturn("PAY123");
        Optional<PaymentResponse> paymentSession = buildPaymentResponse("orderable-item", "productA");
        when(paymentsApiClient.getPaymentSession("PAY123")).thenReturn(paymentSession);
        when(paymentsApiClient.getPaymentDetails("PAY123")).thenReturn(null);
        try {
            router.route(paymentReconciliation);
            Assertions.fail("Expected RetryableErrorException");
        } catch (Exception e) {
            Assertions.assertThat(e).isInstanceOf(RetryableErrorException.class);
        }
    }

    @Test
    void route_doesNothing_whenPaymentStatusIsNull() {
        when(paymentReconciliation.getPaymentResourceId()).thenReturn("PAY123");
        Optional<PaymentResponse> paymentSession = buildPaymentResponse("orderable-item", "productA");
        Map<String, Integer> productCodes = new HashMap<>();
        productCodes.put("productA", 1);
        when(paymentsApiClient.getPaymentSession("PAY123")).thenReturn(paymentSession);
        when(paymentsApiClient.getPaymentDetails("PAY123")).thenReturn(paymentDetails);
        when(productCodeLoader.getProductCodes()).thenReturn(productCodes);
        when(paymentDetails.getPaymentStatus()).thenReturn(null);
        router.route(paymentReconciliation);
        verifyNoInteractions(standardTransactionHandler, refundTransactionHandler);
    }

    @Test
    void route_doesNothing_whenPaymentSessionCostsIsNull() {
        when(paymentReconciliation.getPaymentResourceId()).thenReturn("PAY123");
        PaymentResponse paymentResponse = new PaymentResponse();
        paymentResponse.setCosts(null);
        when(paymentsApiClient.getPaymentSession("PAY123")).thenReturn(Optional.of(paymentResponse));
        when(paymentsApiClient.getPaymentDetails("PAY123")).thenReturn(paymentDetails);
        router.route(paymentReconciliation);
        verifyNoInteractions(standardTransactionHandler, refundTransactionHandler);
    }

    @Test
    void route_doesNothing_whenPaymentSessionCostsIsEmpty() {
        when(paymentReconciliation.getPaymentResourceId()).thenReturn("PAY123");
        PaymentResponse paymentResponse = new PaymentResponse();
        paymentResponse.setCosts(Collections.emptyList());
        when(paymentsApiClient.getPaymentSession("PAY123")).thenReturn(Optional.of(paymentResponse));
        when(paymentsApiClient.getPaymentDetails("PAY123")).thenReturn(paymentDetails);
        router.route(paymentReconciliation);
        verifyNoInteractions(standardTransactionHandler, refundTransactionHandler);
    }

    @Test
    void route_doesNothing_whenPaymentSessionCostsFirstIsNull() {
        when(paymentReconciliation.getPaymentResourceId()).thenReturn("PAY123");
        PaymentResponse paymentResponse = new PaymentResponse();
        paymentResponse.setCosts(Collections.singletonList(null));
        when(paymentsApiClient.getPaymentSession("PAY123")).thenReturn(Optional.of(paymentResponse));
        when(paymentsApiClient.getPaymentDetails("PAY123")).thenReturn(paymentDetails);
        router.route(paymentReconciliation);
        verifyNoInteractions(standardTransactionHandler, refundTransactionHandler);
    }

    @Test
    void route_doesNothing_whenClassOfPaymentIsNull() {
        when(paymentReconciliation.getPaymentResourceId()).thenReturn("PAY123");
        Cost cost = new Cost();
        cost.setClassOfPayment(null);
        cost.setProductType("productA");
        PaymentResponse paymentResponse = new PaymentResponse();
        paymentResponse.setCosts(Collections.singletonList(cost));
        when(paymentsApiClient.getPaymentSession("PAY123")).thenReturn(Optional.of(paymentResponse));
        when(paymentsApiClient.getPaymentDetails("PAY123")).thenReturn(paymentDetails);
        when(paymentsApiClient.getPaymentSession("PAY123")).thenReturn(Optional.of(paymentResponse));
        when(paymentsApiClient.getPaymentDetails("PAY123")).thenReturn(paymentDetails);
        router.route(paymentReconciliation);
        verifyNoInteractions(standardTransactionHandler, refundTransactionHandler);
    }

    @Test
    void route_doesNothing_whenClassOfPaymentIsEmpty() {
        when(paymentReconciliation.getPaymentResourceId()).thenReturn("PAY123");
        Cost cost = new Cost();
        cost.setClassOfPayment(Collections.emptyList());
        cost.setProductType("productA");
        PaymentResponse paymentResponse = new PaymentResponse();
        paymentResponse.setCosts(Collections.singletonList(cost));
        when(paymentsApiClient.getPaymentSession("PAY123")).thenReturn(Optional.of(paymentResponse));
        when(paymentsApiClient.getPaymentDetails("PAY123")).thenReturn(paymentDetails);
        router.route(paymentReconciliation);
        verifyNoInteractions(standardTransactionHandler, refundTransactionHandler);
    }

    @Test
    void route_doesNothing_whenProductTypeIsNull() {
        when(paymentReconciliation.getPaymentResourceId()).thenReturn("PAY123");
        Cost cost = new Cost();
        cost.setClassOfPayment(Collections.singletonList("classA"));
        cost.setProductType(null);
        PaymentResponse paymentResponse = new PaymentResponse();
        paymentResponse.setCosts(Collections.singletonList(cost));
        when(paymentsApiClient.getPaymentSession("PAY123")).thenReturn(Optional.of(paymentResponse));
        when(paymentsApiClient.getPaymentDetails("PAY123")).thenReturn(paymentDetails);
        router.route(paymentReconciliation);
        verifyNoInteractions(standardTransactionHandler, refundTransactionHandler);
    }

    @Test
    void route_doesNothing_whenPaymentSessionOptionalIsEmpty() {
        when(paymentReconciliation.getPaymentResourceId()).thenReturn("PAY123");
        when(paymentsApiClient.getPaymentSession("PAY123")).thenReturn(Optional.empty());
        router.route(paymentReconciliation);
        verifyNoInteractions(standardTransactionHandler, refundTransactionHandler);
    }

    @Test
    void route_doesNothing_whenPaymentResourceIdIsNull() {
        when(paymentReconciliation.getPaymentResourceId()).thenReturn(null);
        when(paymentsApiClient.getPaymentSession(null)).thenReturn(Optional.empty());
        router.route(paymentReconciliation);
        verifyNoInteractions(standardTransactionHandler, refundTransactionHandler);
    }

    @Test
    void route_handlesStandardTransaction_whenRefundIdIsEmptyString() {
        when(paymentReconciliation.getPaymentResourceId()).thenReturn("PAY123");
        when(paymentReconciliation.getRefundId()).thenReturn("");
        Optional<PaymentResponse> paymentSession = buildPaymentResponse("orderable-item", "productB");
        Map<String, Integer> productCodes = new HashMap<>();
        productCodes.put("productB", 2);
        when(paymentsApiClient.getPaymentSession("PAY123")).thenReturn(paymentSession);
        when(paymentsApiClient.getPaymentDetails("PAY123")).thenReturn(paymentDetails);
        when(productCodeLoader.getProductCodes()).thenReturn(productCodes);
        when(paymentDetails.getPaymentStatus()).thenReturn("accepted");
        router.route(paymentReconciliation);
        verify(standardTransactionHandler).handle(paymentDetails, paymentSession.get(), "PAY123");
        verifyNoInteractions(refundTransactionHandler);
    }

    @Test
    void route_doesNothing_whenClassOfPaymentContainsEmptyString() {
        when(paymentReconciliation.getPaymentResourceId()).thenReturn("PAY123");
        Cost cost = new Cost();
        cost.setClassOfPayment(Collections.singletonList(""));
        cost.setProductType("productA");
        PaymentResponse paymentResponse = new PaymentResponse();
        paymentResponse.setCosts(Collections.singletonList(cost));
        when(paymentsApiClient.getPaymentSession("PAY123")).thenReturn(Optional.of(paymentResponse));
        when(paymentsApiClient.getPaymentDetails("PAY123")).thenReturn(paymentDetails);
        router.route(paymentReconciliation);
        verifyNoInteractions(standardTransactionHandler, refundTransactionHandler);
    }
}