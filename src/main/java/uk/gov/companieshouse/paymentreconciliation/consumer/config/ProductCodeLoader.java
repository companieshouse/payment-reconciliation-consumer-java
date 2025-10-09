package uk.gov.companieshouse.paymentreconciliation.consumer.config;

import static uk.gov.companieshouse.paymentreconciliation.consumer.Application.NAMESPACE;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import javax.annotation.PostConstruct;

import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.paymentreconciliation.consumer.exception.ProductCodesNotFoundException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;

@Service
public class ProductCodeLoader {

    @Value("classpath:product_codes.yml")
    private Resource resource;

    private Map<String, Integer> productCodes;

    private static final Logger LOGGER = LoggerFactory.getLogger(NAMESPACE);

    @PostConstruct
    public void init() {
        Yaml yaml = new Yaml();
        try (InputStream inputStream = resource.getInputStream()) {
            Map<String,Object> yamlData = yaml.load(inputStream);
            this.productCodes = (Map<String, Integer>) yamlData.get("product_code");
        } catch (IOException e) {
            LOGGER.error("Failed to load product codes", e);
            throw new ProductCodesNotFoundException("Failed to load product codes", e);
        }
    }

    public Map<String, Integer> getProductCodes() {
        return productCodes;
    }
}