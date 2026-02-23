package uk.gov.companieshouse.paymentreconciliation.consumer.apiclient;

import static uk.gov.companieshouse.paymentreconciliation.consumer.Application.NAMESPACE;

import java.util.Optional;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.payment.PaymentResponse;
import uk.gov.companieshouse.api.model.payment.RefundModel;
import uk.gov.companieshouse.api.payments.PaymentDetailsResponse;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.paymentreconciliation.consumer.logging.DataMapHolder;


@Component
public class PaymentsApiClient {

    private final Supplier<InternalApiClient> internalApiClientFactory;
    private final ResponseHandler responseHandler;
    private final String skipGoneResourceId;
    private final Boolean skipGoneResource;

    private static final String GET_PAYMENT_SESSION_URI = "/payments/%s";
    private static final String GET_PAYMENT_DETAILS_URI = "/private/payments/%s/payment-details";
    private static final String GET_LATEST_REFUND_STATUS_URI = "/payments/%s/refunds/%s";

    private static final Logger LOGGER = LoggerFactory.getLogger(NAMESPACE);

    public PaymentsApiClient(Supplier<InternalApiClient> internalApiClientFactory, ResponseHandler responseHandler,
    @Value("${skip.gone.resource.id}")String skipGoneResourceId, @Value("${skip.gone.resource}")   Boolean skipGoneResource) {
        this.internalApiClientFactory = internalApiClientFactory;
        this.responseHandler = responseHandler;
        this.skipGoneResourceId = skipGoneResourceId;
        this.skipGoneResource = skipGoneResource;
    }

    public Optional<PaymentResponse> getPaymentSession(String paymentId){
        InternalApiClient client = internalApiClientFactory.get();
        Optional<PaymentResponse> response = Optional.empty();
        try {
            LOGGER.info("Getting payment session for paymentId: %s".formatted(paymentId), DataMapHolder.getLogMap());
            String requestUri = GET_PAYMENT_SESSION_URI.formatted(paymentId);
            return Optional.ofNullable(client.privatePayment().getPaymentSession(requestUri).execute().getData());
        } catch (ApiErrorResponseException ex) {
            LOGGER.error(String.format("Unable to obtain response from %s for resource ID: %s", GET_PAYMENT_SESSION_URI, paymentId), DataMapHolder.getLogMap());
            if (ex.getStatusCode() == HttpStatus.GONE.value() && checkSkipGoneResource(paymentId)) {
                LOGGER.info(String.format("Skipping message for Payment ID [%s] due to GONE response and SKIP_GONE_RESOURCE configuration", paymentId), DataMapHolder.getLogMap());
                return Optional.empty();
            }
            responseHandler.handle(GET_PAYMENT_SESSION_URI, paymentId, ex);
        } catch (URIValidationException ex) {
            responseHandler.handle(ex);
        }
        return response;
    }

    public PaymentDetailsResponse getPaymentDetails(String paymentId){
        InternalApiClient client = internalApiClientFactory.get();
        try {
            LOGGER.info("Getting payment details for paymentId: %s".formatted(paymentId), DataMapHolder.getLogMap());
            String requestUri = GET_PAYMENT_DETAILS_URI.formatted(paymentId);
            ApiResponse<PaymentDetailsResponse> response = client.privatePayment().getPaymentDetails(requestUri).execute();
            return response.getData();
        } catch (ApiErrorResponseException ex) {
            responseHandler.handle(GET_PAYMENT_DETAILS_URI, paymentId, ex);
        } catch (URIValidationException ex) {
            responseHandler.handle(ex);
        }
        return null;
    }

    public RefundModel patchLatestRefundStatus(String paymentId, RefundModel refund) {
        InternalApiClient client = internalApiClientFactory.get();
        try {
            LOGGER.info("Patching latest refund status for paymentId: %s and refundId: %s".formatted(paymentId, refund.getRefundId()), DataMapHolder.getLogMap());
            String requestUri = GET_LATEST_REFUND_STATUS_URI.formatted(paymentId, refund.getRefundId());
            ApiResponse<RefundModel> response = client.privatePayment().patchLatestRefundStatus(requestUri, refund).execute();
            return response.getData();
        } catch (ApiErrorResponseException ex) {
            responseHandler.handle( GET_LATEST_REFUND_STATUS_URI, paymentId, ex);
        } catch (URIValidationException ex) {
            responseHandler.handle(ex);
        }
		return null;
    }

    private boolean checkSkipGoneResource(String paymentId) {

        if (!Boolean.TRUE.equals(skipGoneResource)) {
            return false;
        }
        if (skipGoneResourceId == null || skipGoneResourceId.isEmpty()) {
            return false;
        }
        if (skipGoneResourceId.equals(paymentId)) {
            LOGGER.info(String.format("SKIP_GONE_RESOURCE_ID [%s] matches Payment ID [%s] - skipping message", skipGoneResourceId, paymentId), DataMapHolder.getLogMap());
            return true;
        }
        return false;
    }
}
