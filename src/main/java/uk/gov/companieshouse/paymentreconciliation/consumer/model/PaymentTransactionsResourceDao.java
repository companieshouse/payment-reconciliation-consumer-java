package uk.gov.companieshouse.paymentreconciliation.consumer.model;

import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "payment_transaction")
public class PaymentTransactionsResourceDao {

    private String transactionId;
    private String transactionDate;
    private String email;
    private String paymentMethod;
    private String amount;
    private String companyNumber;
    private String transactionType;
    private String orderReference;
    private String status;
    private String userId;
    private String originalReference;
    private String disputeDetails;
    public String getTransactionId() {
        return transactionId;
    }
    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }
    public String getTransactionDate() {
        return transactionDate;
    }
    public void setTransactionDate(String transactionDate) {
        this.transactionDate = transactionDate;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getPaymentMethod() {
        return paymentMethod;
    }
    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
    public String getAmount() {
        return amount;
    }
    public void setAmount(String amount) {
        this.amount = amount;
    }
    public String getCompanyNumber() {
        return companyNumber;
    }
    public void setCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
    }
    public String getTransactionType() {
        return transactionType;
    }
    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }
    public String getOrderReference() {
        return orderReference;
    }
    public void setOrderReference(String orderReference) {
        this.orderReference = orderReference;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }
    public String getOriginalReference() {
        return originalReference;
    }
    public void setOriginalReference(String originalReference) {
        this.originalReference = originalReference;
    }
    public String getDisputeDetails() {
        return disputeDetails;
    }
    public void setDisputeDetails(String disputeDetails) {
        this.disputeDetails = disputeDetails;
    }
}
