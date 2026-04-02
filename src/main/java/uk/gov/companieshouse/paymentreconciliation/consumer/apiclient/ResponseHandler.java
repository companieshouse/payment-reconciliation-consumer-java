package uk.gov.companieshouse.paymentreconciliation.consumer.apiclient;

import static uk.gov.companieshouse.paymentreconciliation.consumer.Application.NAMESPACE;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.paymentreconciliation.consumer.exception.NonRetryableException;
import uk.gov.companieshouse.paymentreconciliation.consumer.exception.RetryableException;
import uk.gov.companieshouse.paymentreconciliation.consumer.logging.DataMapHolder;

@Component
public class ResponseHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(NAMESPACE);
    private static final String API_ERROR_RESPONSE_MESSAGE = "%s failed, resource URI: %s, status code: %d.";
    private static final String URI_VALIDATION_EXCEPTION_MESSAGE = "Invalid URI";

    public void handle(String apiCall, String resourceUri, ApiErrorResponseException ex) {
        final int statusCode = ex.getStatusCode();
        if (HttpStatus.BAD_REQUEST.value() == statusCode || HttpStatus.CONFLICT.value() == statusCode) {
            LOGGER.error(String.format(API_ERROR_RESPONSE_MESSAGE, apiCall, resourceUri, statusCode),
                    ex, DataMapHolder.getLogMap());
            throw new NonRetryableException(String.format(API_ERROR_RESPONSE_MESSAGE, apiCall, resourceUri, statusCode), ex);
        } else {
            LOGGER.error(String.format(API_ERROR_RESPONSE_MESSAGE, apiCall, resourceUri, ex.getStatusCode()),ex, DataMapHolder.getLogMap());
            throw new RetryableException(String.format(API_ERROR_RESPONSE_MESSAGE, apiCall, resourceUri, statusCode), ex);
        }
    }

    public void handle(URIValidationException ex) {
        LOGGER.error(ex.getMessage(), DataMapHolder.getLogMap());
        LOGGER.error(URI_VALIDATION_EXCEPTION_MESSAGE, DataMapHolder.getLogMap());
        throw new NonRetryableException(URI_VALIDATION_EXCEPTION_MESSAGE, ex);
    }
}
