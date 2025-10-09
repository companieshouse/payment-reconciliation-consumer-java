package uk.gov.companieshouse.paymentreconciliation.consumer.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import uk.gov.companieshouse.paymentreconciliation.consumer.model.PaymentTransactionsResourceDao;

@Repository
public interface TransactionRepository extends MongoRepository<PaymentTransactionsResourceDao, String> {

}