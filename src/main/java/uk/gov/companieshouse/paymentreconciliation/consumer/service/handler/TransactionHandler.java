package uk.gov.companieshouse.paymentreconciliation.consumer.service.handler;

public interface TransactionHandler<T, U> {
    void handle(T message, U args);
}
