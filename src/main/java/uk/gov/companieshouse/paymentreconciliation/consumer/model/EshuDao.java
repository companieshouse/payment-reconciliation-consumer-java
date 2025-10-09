package uk.gov.companieshouse.paymentreconciliation.consumer.model;

import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "eshu")
public class EshuDao {
    String paymentRef;
    int productCode;
    String companyNumber;
    String filingDate;
    String madeUpdate;
    String transactionDate;

    public String getPaymentRef() {
        return paymentRef;
    }
    public void setPaymentRef(String paymentRef) {
        this.paymentRef = paymentRef;
    }
    public int getProductCode() {
        return productCode;
    }
    public void setProductCode(int productCode) {
        this.productCode = productCode;
    }
    public String getCompanyNumber() {
        return companyNumber;
    }
    public void setCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
    }
    public String getFilingDate() {
        return filingDate;
    }
    public void setFilingDate(String filingDate) {
        this.filingDate = filingDate;
    }
    public String getMadeUpdate() {
        return madeUpdate;
    }
    public void setMadeUpdate(String madeUpdate) {
        this.madeUpdate = madeUpdate;
    }
    public String getTransactionDate() {
        return transactionDate;
    }
    public void setTransactionDate(String transactionDate) {
        this.transactionDate = transactionDate;
    }

}
