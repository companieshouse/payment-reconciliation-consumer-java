package uk.gov.companieshouse.paymentreconciliation.consumer.mapper;

import uk.gov.companieshouse.api.payments.PaymentResponse;
import uk.gov.companieshouse.paymentreconciliation.consumer.config.ProductCodeLoader;
import uk.gov.companieshouse.paymentreconciliation.consumer.model.EshuDao;
import java.util.List;
import java.util.ArrayList;

import org.springframework.stereotype.Component;

@Component
public class EshuMapper {
    private final ProductCodeLoader productCodeLoader;
    public EshuMapper(ProductCodeLoader productCodeLoader) {
        this.productCodeLoader = productCodeLoader;
    }
    public List<EshuDao> mapFromPaymentResponse(PaymentResponse paymentResponse, String paymentId, String transactionDate) {
        List<EshuDao> eshuResources = new ArrayList<>();
        for (var cost : paymentResponse.getCosts()) {
            String productType = cost.getProductType();
            int productCode = productCodeLoader.getProductCodes().get(productType);
            EshuDao eshu = new EshuDao();
            eshu.setPaymentRef("X" + paymentId);
            eshu.setProductCode(productCode);
            eshu.setCompanyNumber(paymentResponse.getCompanyNumber());
            eshu.setFilingDate("");
            eshu.setMadeUpdate("");
            eshu.setTransactionDate(transactionDate);
            eshuResources.add(eshu);
        }
        return eshuResources;
    }
}
