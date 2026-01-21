package uk.gov.companieshouse.paymentreconciliation.consumer.apiclient;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.function.Supplier;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
import uk.gov.companieshouse.api.payments.PaymentDetailsResponse;
import uk.gov.companieshouse.api.model.payment.PaymentResponse;
import uk.gov.companieshouse.api.model.payment.RefundModel;
import uk.gov.companieshouse.api.payments.Refund;

class PaymentsApiClientTest {

    private Supplier<InternalApiClient> internalApiClientFactory;
    private ResponseHandler responseHandler;
    private InternalApiClient internalApiClient;
    private PrivatePaymentResourceHandler privatePaymentResourceHandler;

    private PaymentsApiClient paymentsApiClient;

    @BeforeEach
    void setUp() {
        internalApiClientFactory = mock(Supplier.class);
        responseHandler = mock(ResponseHandler.class);
        internalApiClient = mock(InternalApiClient.class);
        privatePaymentResourceHandler = mock(PrivatePaymentResourceHandler.class);

        when(internalApiClientFactory.get()).thenReturn(internalApiClient);
        when(internalApiClient.privatePayment()).thenReturn(privatePaymentResourceHandler);
        when(privatePaymentResourceHandler.getPaymentSession(anyString()))
                .thenReturn(mock(PaymentGetPaymentSession.class));
        when(privatePaymentResourceHandler.getPaymentDetails(anyString()))
                .thenReturn(mock(PaymentGetPaymentDetails.class));
        when(privatePaymentResourceHandler.patchLatestRefundStatus(anyString(), any(RefundModel.class)))
                .thenReturn(mock(PaymentPatchRefundStatus.class));
        paymentsApiClient = new PaymentsApiClient(internalApiClientFactory, responseHandler);
    }

    @Test
    void getPaymentSession_returnsPaymentResponse_onSuccess() throws Exception {
        String paymentId = "123";
        PaymentResponse paymentResponse = new PaymentResponse();
        var paymentGetPaymentSession = mock(PaymentGetPaymentSession.class);
        var apiResponse = mock(ApiResponse.class);

        when(privatePaymentResourceHandler.getPaymentSession("/payments/123")).thenReturn(paymentGetPaymentSession);
        when(paymentGetPaymentSession.execute()).thenReturn(apiResponse);
        when(apiResponse.getData()).thenReturn(paymentResponse);

        PaymentResponse result = paymentsApiClient.getPaymentSession(paymentId);

        assertSame(paymentResponse, result);
    }

    @Test
    void getPaymentSession_handlesApiErrorResponseException() throws Exception {
        String paymentId = "123";
        var paymentGetPaymentSession = mock(PaymentGetPaymentSession.class);

        when(privatePaymentResourceHandler.getPaymentSession("/payments/123")).thenReturn(paymentGetPaymentSession);
        when(paymentGetPaymentSession.execute()).thenThrow(ApiErrorResponseException.fromHttpResponseException(new Builder(400, "Bad Request", new HttpHeaders()).build()));

        PaymentResponse result = paymentsApiClient.getPaymentSession(paymentId);

        assertNull(result);
        verify(responseHandler).handle(any(ApiErrorResponseException.class));
    }

    @Test
    void getPaymentSession_handlesURIValidationException() throws Exception {
        String paymentId = "123";
        var paymentGetPaymentSession = mock(PaymentGetPaymentSession.class);

        when(privatePaymentResourceHandler.getPaymentSession("/payments/123")).thenReturn(paymentGetPaymentSession);
        when(paymentGetPaymentSession.execute()).thenThrow(new URIValidationException("invalid uri"));
        PaymentResponse result = paymentsApiClient.getPaymentSession(paymentId);

        assertNull(result);
        verify(responseHandler).handle(any(URIValidationException.class));
    }

    @Test
    void getPaymentDetails_returnsPaymentDetailsResponse_onSuccess() throws Exception {
        String paymentId = "456";
        PaymentDetailsResponse paymentDetailsResponse = new PaymentDetailsResponse();
        var paymentDetailsGet = mock(PaymentGetPaymentDetails.class);
        var apiResponse = mock(ApiResponse.class);

        when(privatePaymentResourceHandler.getPaymentDetails("/private/payments/456/payment-details")).thenReturn(paymentDetailsGet);
        when(paymentDetailsGet.execute()).thenReturn(apiResponse);
        when(apiResponse.getData()).thenReturn(paymentDetailsResponse);

        PaymentDetailsResponse result = paymentsApiClient.getPaymentDetails(paymentId);

        assertSame(paymentDetailsResponse, result);
    }

    @Test
    void getPaymentDetails_handlesApiErrorResponseException() throws Exception {
        String paymentId = "456";
        var paymentGetPaymentDetails = mock(PaymentGetPaymentDetails.class);

        when(privatePaymentResourceHandler.getPaymentDetails("/private/payments/456/payment-details")).thenReturn(paymentGetPaymentDetails);
        when(paymentGetPaymentDetails.execute()).thenThrow(ApiErrorResponseException.fromHttpResponseException(new Builder(400, "Bad Request", new HttpHeaders()).build()));
        PaymentDetailsResponse result = paymentsApiClient.getPaymentDetails(paymentId);

        assertNull(result);
        verify(responseHandler).handle(any(ApiErrorResponseException.class));
    }

    @Test
    void getPaymentDetails_handlesURIValidationException() throws Exception {
        String paymentId = "456";
        var paymentGetPaymentDetails = mock(PaymentGetPaymentDetails.class);

        when(privatePaymentResourceHandler.getPaymentDetails("/private/payments/456/payment-details")).thenReturn(paymentGetPaymentDetails);
        when(paymentGetPaymentDetails.execute()).thenThrow(new URIValidationException("invalid uri"));

        PaymentDetailsResponse result = paymentsApiClient.getPaymentDetails(paymentId);

        assertNull(result);
        verify(responseHandler).handle(any(URIValidationException.class));
    }

    @Test
    void getLatestRefundStatus_returnsRefund_onSuccess() throws Exception {
        String paymentId = "789";
        String refundId = "r1";
        RefundModel refund = new RefundModel();
        refund.setRefundId(refundId);
        var paymentPatchRefundStatus = mock(PaymentPatchRefundStatus.class);
        var apiResponse = mock(ApiResponse.class);

        when(privatePaymentResourceHandler.patchLatestRefundStatus("/payments/789/refunds/r1", refund))
                .thenReturn(paymentPatchRefundStatus);
        when(paymentPatchRefundStatus.execute()).thenReturn(apiResponse);
        when(apiResponse.getData()).thenReturn(refund);

        RefundModel result = paymentsApiClient.patchLatestRefundStatus(paymentId, refund);

        assertSame(refund, result);
    }

    @Test
    void getLatestRefundStatus_handlesApiErrorResponseException() throws Exception {
        String paymentId = "789";
        RefundModel refund =  new RefundModel();
        refund.setRefundId("r1");
        var paymentPatchRefundStatus = mock(PaymentPatchRefundStatus.class);

        when(privatePaymentResourceHandler.patchLatestRefundStatus("/payments/789/refunds/r1", refund))
                .thenReturn(paymentPatchRefundStatus);
        when(paymentPatchRefundStatus.execute()).thenThrow(ApiErrorResponseException.fromHttpResponseException(new Builder(400, "Bad Request", new HttpHeaders() ).build()));

        RefundModel result = paymentsApiClient.patchLatestRefundStatus(paymentId, refund);
        assertNull(result);
        verify(responseHandler).handle(any(ApiErrorResponseException.class));
    }

    @Test
    void getLatestRefundStatus_handlesURIValidationException() throws Exception {
        String paymentId = "789";
        RefundModel refund =  new RefundModel();
        refund.setRefundId("r1");
        var paymentPatchRefundStatus = mock(PaymentPatchRefundStatus.class);

        when(privatePaymentResourceHandler.patchLatestRefundStatus("/payments/789/refunds/r1", refund))
                .thenReturn(paymentPatchRefundStatus);
        when(paymentPatchRefundStatus.execute()).thenThrow(new URIValidationException("invalid uri"));
        RefundModel result = paymentsApiClient.patchLatestRefundStatus(paymentId, refund);

        assertNull(result);
        verify(responseHandler).handle(any(URIValidationException.class));
    }
}