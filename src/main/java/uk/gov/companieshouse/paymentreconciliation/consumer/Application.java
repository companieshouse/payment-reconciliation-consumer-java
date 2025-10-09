package uk.gov.companieshouse.paymentreconciliation.consumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@EnableMongoRepositories
public class Application {

    public static final String NAMESPACE = "payment-reconciliation-consumer-java";
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
