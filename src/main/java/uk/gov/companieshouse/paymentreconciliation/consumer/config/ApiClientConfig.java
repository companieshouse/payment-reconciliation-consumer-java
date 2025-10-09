package uk.gov.companieshouse.paymentreconciliation.consumer.config;

import java.util.function.Supplier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.http.ApiKeyHttpClient;

@Configuration
public class ApiClientConfig {

    @Bean
    Supplier<InternalApiClient> internalApiClientSupplier(
            @Value("${payments.api-key}") String apiKey,
            @Value("${payments.api-url}") String apiUrl) {
        return () -> {
            InternalApiClient internalApiClient = new InternalApiClient(new ApiKeyHttpClient(apiKey));
            internalApiClient.setBasePaymentsPath(apiUrl);
            return internalApiClient;
        };
    }
}
