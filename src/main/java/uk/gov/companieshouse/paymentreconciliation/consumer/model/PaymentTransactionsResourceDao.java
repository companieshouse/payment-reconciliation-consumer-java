package uk.gov.companieshouse.paymentreconciliation.consumer.model;

import java.time.Instant;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "payment_transaction")
public class PaymentTransactionsResourceDao {

    @Field("transaction_id")
    private String transactionId;

    @Field("transaction_date")
    private Instant transactionDate;

    @Field("email")
    private String email;

    @Field("payment_method")
    private String paymentMethod;

    @Field("amount")
    private String amount;

    @Field("company_number")
    private String companyNumber;

    @Field("transaction_type")
    private String transactionType;

    @Field("order_reference")
    private String orderReference;

    @Field("status")
    private String status;

    @Field("user_id")
    private String userId;

    @Field("original_reference")
    private String originalReference;

    @Field("dispute_details")
    private String disputeDetails;

    public String getTransactionId() {
        return transactionId;
    }
    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }
    public Instant getTransactionDate() {
        return transactionDate;
    }
    public void setTransactionDate(Instant transactionDate) {
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
