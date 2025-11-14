package uk.gov.companieshouse.paymentreconciliation.consumer.exception;

public class RetryableException extends RuntimeException {

    public RetryableException(String message, Throwable cause) {
        super(message, cause);
    }
}
