package uk.gov.companieshouse.paymentreconciliation.consumer.apiclient;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;

import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpResponseException.Builder;

import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.handler.payments.PrivatePaymentResourceHandler;
import uk.gov.companieshouse.api.handler.payments.request.PaymentGetPaymentDetails;
import uk.gov.companieshouse.api.handler.payments.request.PaymentGetPaymentSession;
import uk.gov.companieshouse.api.handler.payments.request.PaymentPatchRefundStatus;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.payment.PaymentResponse;
import uk.gov.companieshouse.api.model.payment.RefundModel;
import uk.gov.companieshouse.api.payments.PaymentDetailsResponse;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PaymentsApiClientTest {
    private static final String PAYMENT_ID_SESSION = "123";
    private static final String PAYMENT_ID_DETAILS = "456";
    private static final String PAYMENT_ID_REFUND = "789";
    private static final String REFUND_ID = "r1";

    @Mock
    private Supplier<InternalApiClient> internalApiClientFactory;
    @Mock
    private ResponseHandler responseHandler;
    @Mock
    private InternalApiClient internalApiClient;
    @Mock
    private PrivatePaymentResourceHandler privatePaymentResourceHandler;
    @Mock
    private PaymentGetPaymentSession paymentGetPaymentSession;
    @Mock
    private PaymentGetPaymentDetails paymentGetPaymentDetails;
    @Mock
    private PaymentPatchRefundStatus paymentPatchRefundStatus;
    @Mock
    private ApiResponse<PaymentResponse> apiPaymentResponse;
    @Mock
    private ApiResponse<PaymentDetailsResponse> apiPaymentDetailsResponse;
    @Mock
    private ApiResponse<RefundModel> apiRefundResponse;
    @Mock
    private PaymentsApiClient paymentsApiClient;

    @BeforeEach
    void setUp() {
        when(internalApiClientFactory.get()).thenReturn(internalApiClient);
        when(internalApiClient.privatePayment()).thenReturn(privatePaymentResourceHandler);
        // Default constructor, override in test if needed
        paymentsApiClient = new PaymentsApiClient(internalApiClientFactory, responseHandler, null, null);
    }

    @Test
    void getPaymentSession_returnsPaymentResponse_onSuccess() throws Exception {
        PaymentResponse paymentResponse = new PaymentResponse();

        when(privatePaymentResourceHandler.getPaymentSession("/payments/123")).thenReturn(paymentGetPaymentSession);
        when(paymentGetPaymentSession.execute()).thenReturn(apiPaymentResponse);
        when(apiPaymentResponse.getData()).thenReturn(paymentResponse);

        Optional<PaymentResponse> result = paymentsApiClient.getPaymentSession(PAYMENT_ID_SESSION);


        assertTrue(result.isPresent());
        assertSame(paymentResponse, result.get());
    }

    @Test
    void getPaymentSession_handlesApiErrorResponseException() throws Exception {
        when(privatePaymentResourceHandler.getPaymentSession("/payments/123")).thenReturn(paymentGetPaymentSession);
        when(paymentGetPaymentSession.execute()).thenThrow(ApiErrorResponseException.fromHttpResponseException(new Builder(400, "Bad Request", new HttpHeaders()).build()));

        Optional<PaymentResponse> result = paymentsApiClient.getPaymentSession(PAYMENT_ID_SESSION);

        assertTrue(result.isEmpty());
        verify(responseHandler).handle(any(String.class), any(String.class),any(ApiErrorResponseException.class));
    }

    @Test
    void getPaymentSession_handlesURIValidationException() throws Exception {
        when(privatePaymentResourceHandler.getPaymentSession("/payments/123")).thenReturn(paymentGetPaymentSession);
        when(paymentGetPaymentSession.execute()).thenThrow(new URIValidationException("invalid uri"));
        Optional<PaymentResponse> result = paymentsApiClient.getPaymentSession(PAYMENT_ID_SESSION);

        assertTrue(result.isEmpty());
        verify(responseHandler).handle(any(URIValidationException.class));
    }

    @Test
    void getPaymentDetails_returnsPaymentDetailsResponse_onSuccess() throws Exception {
        PaymentDetailsResponse paymentDetailsResponse = new PaymentDetailsResponse();
        var paymentDetailsGet = this.paymentGetPaymentDetails;

        when(privatePaymentResourceHandler.getPaymentDetails("/private/payments/456/payment-details")).thenReturn(paymentDetailsGet);
        when(paymentDetailsGet.execute()).thenReturn(apiPaymentDetailsResponse);
        when(apiPaymentDetailsResponse.getData()).thenReturn(paymentDetailsResponse);

        PaymentDetailsResponse result = paymentsApiClient.getPaymentDetails(PAYMENT_ID_DETAILS);
        assertSame(paymentDetailsResponse, result);
    }

    @Test
    void getPaymentDetails_handlesApiErrorResponseException() throws Exception {
        when(privatePaymentResourceHandler.getPaymentDetails("/private/payments/456/payment-details")).thenReturn(paymentGetPaymentDetails);
        when(paymentGetPaymentDetails.execute()).thenThrow(ApiErrorResponseException.fromHttpResponseException(new Builder(400, "Bad Request", new HttpHeaders()).build()));
        PaymentDetailsResponse result = paymentsApiClient.getPaymentDetails(PAYMENT_ID_DETAILS);

        assertNull(result);
        verify(responseHandler).handle(any(String.class), any(String.class), any(ApiErrorResponseException.class));
    }

    @Test
    void getPaymentDetails_handlesURIValidationException() throws Exception {
        when(privatePaymentResourceHandler.getPaymentDetails("/private/payments/456/payment-details")).thenReturn(paymentGetPaymentDetails);
        when(paymentGetPaymentDetails.execute()).thenThrow(new URIValidationException("invalid uri"));

        PaymentDetailsResponse result = paymentsApiClient.getPaymentDetails(PAYMENT_ID_DETAILS);

        assertNull(result);
        verify(responseHandler).handle(any(URIValidationException.class));
    }

    @Test
    void getLatestRefundStatus_returnsRefund_onSuccess() throws Exception {
        String paymentId = PAYMENT_ID_REFUND;
        String refundId = REFUND_ID;
        RefundModel refund = new RefundModel();
        refund.setRefundId(refundId);

        when(privatePaymentResourceHandler.patchLatestRefundStatus("/payments/789/refunds/r1", refund))
                .thenReturn(paymentPatchRefundStatus);
        when(paymentPatchRefundStatus.execute()).thenReturn(apiRefundResponse);
        when(apiRefundResponse.getData()).thenReturn(refund);

        RefundModel result = paymentsApiClient.patchLatestRefundStatus(paymentId, refund);

        assertSame(refund, result);
    }

    @Test
    void getLatestRefundStatus_handlesApiErrorResponseException() throws Exception {
        String paymentId = PAYMENT_ID_REFUND;
        RefundModel refund =  new RefundModel();
        refund.setRefundId("r1");

        when(privatePaymentResourceHandler.patchLatestRefundStatus("/payments/789/refunds/r1", refund))
                .thenReturn(paymentPatchRefundStatus);
        when(paymentPatchRefundStatus.execute()).thenThrow(ApiErrorResponseException.fromHttpResponseException(new Builder(400, "Bad Request", new HttpHeaders() ).build()));

        RefundModel result = paymentsApiClient.patchLatestRefundStatus(paymentId, refund);
        assertNull(result);
        verify(responseHandler).handle(any(String.class), any(String.class), any(ApiErrorResponseException.class));
    }

    @Test
    void getLatestRefundStatus_handlesURIValidationException() throws Exception {
        String paymentId = PAYMENT_ID_REFUND;
        RefundModel refund =  new RefundModel();
        refund.setRefundId("r1");

        when(privatePaymentResourceHandler.patchLatestRefundStatus("/payments/789/refunds/r1", refund))
                .thenReturn(paymentPatchRefundStatus);
        when(paymentPatchRefundStatus.execute()).thenThrow(new URIValidationException("invalid uri"));
        RefundModel result = paymentsApiClient.patchLatestRefundStatus(paymentId, refund);

        assertNull(result);
        verify(responseHandler).handle(any(URIValidationException.class));
    }

    @Test
    void getPaymentSession_skipsGoneResource_whenConfigured() throws Exception {
        String paymentId = "gone-id";
        when(privatePaymentResourceHandler.getPaymentSession("/payments/gone-id")).thenReturn(paymentGetPaymentSession);
        // Simulate 410 GONE error
        ApiErrorResponseException goneException = ApiErrorResponseException.fromHttpResponseException(new Builder(410, "Gone", new HttpHeaders()).build());
        when(paymentGetPaymentSession.execute()).thenThrow(goneException);

        // skipGoneResource = true, skipGoneResourceId = gone-id
        PaymentsApiClient client = new PaymentsApiClient(internalApiClientFactory, responseHandler, "gone-id", true);
        var result = client.getPaymentSession(paymentId);
        // Should skip and return Optional.empty()
        assertSame(Optional.empty(), result);
    }

    @Test
    void getPaymentSession_doesNotSkipGoneResource_whenIdDoesNotMatch() throws Exception {
        String paymentId = "not-matching-id";
        when(privatePaymentResourceHandler.getPaymentSession("/payments/not-matching-id")).thenReturn(paymentGetPaymentSession);
        ApiErrorResponseException goneException = ApiErrorResponseException.fromHttpResponseException(new Builder(410, "Gone", new HttpHeaders()).build());
        when(paymentGetPaymentSession.execute()).thenThrow(goneException);

        PaymentsApiClient client = new PaymentsApiClient(internalApiClientFactory, responseHandler, "some-other-id", true);
        var result = client.getPaymentSession(paymentId);
        // Should not skip, should call responseHandler and return Optional.empty()
        assertSame(Optional.empty().getClass(), result.getClass()); // result is Optional.empty()
        verify(responseHandler).handle(any(String.class), any(String.class), any(ApiErrorResponseException.class));
    }

    @ParameterizedTest
    @MethodSource("provideSkipGoneResourceCases")
    void getPaymentSession_skipGoneResourceParameterized(String skipGoneResourceId, boolean skipGoneResource, boolean shouldSkip) throws Exception {
        String paymentId = "gone-id";
        when(privatePaymentResourceHandler.getPaymentSession("/payments/gone-id")).thenReturn(paymentGetPaymentSession);
        ApiErrorResponseException goneException = ApiErrorResponseException.fromHttpResponseException(new Builder(410, "Gone", new HttpHeaders()).build());
        when(paymentGetPaymentSession.execute()).thenThrow(goneException);

        PaymentsApiClient client = new PaymentsApiClient(internalApiClientFactory, responseHandler, skipGoneResourceId, skipGoneResource);
        var result = client.getPaymentSession(paymentId);

        if (shouldSkip) {
            assertSame(Optional.empty(), result);
        } else {
            assertTrue(result.isEmpty());
            verify(responseHandler).handle(any(String.class), any(String.class), any(ApiErrorResponseException.class));
        }
    }

    private static Stream<Arguments> provideSkipGoneResourceCases() {
        return Stream.of(
            Arguments.of("gone-id", false, false),
            Arguments.of("", true, false),
            Arguments.of(null, true, false)
        );
    }
}