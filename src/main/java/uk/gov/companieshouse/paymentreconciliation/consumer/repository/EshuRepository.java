package uk.gov.companieshouse.paymentreconciliation.consumer.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import uk.gov.companieshouse.paymentreconciliation.consumer.model.EshuDao;

@Repository
public interface EshuRepository extends MongoRepository<EshuDao, String> {

}