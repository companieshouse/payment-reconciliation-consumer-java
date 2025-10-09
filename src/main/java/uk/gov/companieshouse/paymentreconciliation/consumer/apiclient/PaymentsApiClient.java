package uk.gov.companieshouse.paymentreconciliation.consumer.apiclient;

import static uk.gov.companieshouse.paymentreconciliation.consumer.Application.NAMESPACE;

import java.util.function.Supplier;

import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import org.springframework.stereotype.Component;

import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.payments.PaymentDetailsResponse;
import uk.gov.companieshouse.api.payments.PaymentResponse;
import uk.gov.companieshouse.api.payments.Refund;


@Component
public class PaymentsApiClient {

    private final Supplier<InternalApiClient> internalApiClientFactory;
    private final ResponseHandler responseHandler;

    private static final String GET_PAYMENT_SESSION_URI = "/payments/%s";
    private static final String GET_PAYMENT_DETAILS_URI = "/private/payments/%s/payment-details";
    private static final String GET_LATEST_REFUND_STATUS_URI = "/payments/%s/refunds/%s";

    private static final Logger LOGGER = LoggerFactory.getLogger(NAMESPACE);

    public PaymentsApiClient(Supplier<InternalApiClient> internalApiClientFactory, ResponseHandler responseHandler) {
        this.internalApiClientFactory = internalApiClientFactory;
        this.responseHandler = responseHandler;
    }

    public PaymentResponse getPaymentSession(String paymentId){
        InternalApiClient client = internalApiClientFactory.get();
        try {
            LOGGER.info("Getting payment session for paymentId: %s".formatted(paymentId));
            String requestUri = GET_PAYMENT_SESSION_URI.formatted(paymentId);
            return client.privatePayment().getPaymentSession(requestUri).execute().getData();
        } catch (ApiErrorResponseException ex) {
            responseHandler.handle(ex);
        } catch (URIValidationException ex) {
            responseHandler.handle(ex);
        }
        return null;
    }

    public PaymentDetailsResponse getPaymentDetails(String paymentId){
        InternalApiClient client = internalApiClientFactory.get();
        try {
            LOGGER.info("Getting payment details for paymentId: %s".formatted(paymentId));
            String requestUri = GET_PAYMENT_DETAILS_URI.formatted(paymentId);
            ApiResponse<PaymentDetailsResponse> response = client.privatePayment().getPaymentDetails(requestUri).execute();
            return response.getData();
        } catch (ApiErrorResponseException ex) {
            responseHandler.handle(ex);
        } catch (URIValidationException ex) {
            responseHandler.handle(ex);
        }
        return null;
    }

    public Refund patchLatestRefundStatus(String paymentId, Refund refund) {
        InternalApiClient client = internalApiClientFactory.get();
        try {
            LOGGER.info("Patching latest refund status for paymentId: %s and refundId: %s".formatted(paymentId, refund.getRefundId()));
            String requestUri = GET_LATEST_REFUND_STATUS_URI.formatted(paymentId, refund.getRefundId());
            ApiResponse<Refund> response = client.privatePayment().patchLatestRefundStatus(requestUri, refund).execute();
            return response.getData();
        } catch (ApiErrorResponseException ex) {
            responseHandler.handle(ex);
        } catch (URIValidationException ex) {
            responseHandler.handle(ex);
        }
		return null;
    }

}
