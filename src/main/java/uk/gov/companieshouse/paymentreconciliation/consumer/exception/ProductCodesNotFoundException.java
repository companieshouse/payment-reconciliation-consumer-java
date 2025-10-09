package uk.gov.companieshouse.paymentreconciliation.consumer.exception;

public class ProductCodesNotFoundException extends RuntimeException {

    public ProductCodesNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProductCodesNotFoundException(String message) {
        super(message);
    }
}
