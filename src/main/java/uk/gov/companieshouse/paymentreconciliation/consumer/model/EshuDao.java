package uk.gov.companieshouse.paymentreconciliation.consumer.model;

import org.springframework.data.mongodb.core.mapping.Document;

import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "eshu")
public class EshuDao {

    @Field("payment_reference")
    String paymentReference;

    @Field("product_code")
    Integer productCode;

    @Field("company_number")
    String companyNumber;

    @Field("filing_date")
    String filingDate;

    @Field("made_up_date")
    String madeUpDate;

    @Field("transaction_date")
    String transactionDate;


    public String getPaymentReference() {
        return paymentReference;
    }
    public void setPaymentReference(String paymentReference) {
        this.paymentReference = paymentReference;
    }
    public Integer getProductCode() {
        return productCode;
    }
    public void setProductCode(Integer productCode) {
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
    public String getMadeUpDate() {
        return madeUpDate;
    }
    public void setMadeUpDate(String madeUpDate) {
        this.madeUpDate = madeUpDate;
    }
    public String getTransactionDate() {
        return transactionDate;
    }
    public void setTransactionDate(String transactionDate) {
        this.transactionDate = transactionDate;
    }

}
