package uk.gov.companieshouse.paymentreconciliation.consumer.model;

import java.time.Instant;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "refunds")
public class RefundDao {

    @Field("transaction_id")
    String transactionId;

    @Field("transaction_date")
    Instant transactionDate;

    @Field("email")
    String email;

    @Field("payment_method")
    String paymentMethod;

    @Field("amount")
    String amount;

    @Field  ("company_number")
    String companyNumber;

    @Field("transaction_type")
    String transactionType;

    @Field("order_reference")
    String orderReference;

    @Field("status")
    String status;

    @Field("user_id")
    String userId;

    @Field("original_reference")
    String originalReference;

    @Field("dispute_details")
    String disputeDetails;

    @Field("product_code")
    Integer productCode;

    @Field("payment_id")
    String paymentId;

    @Field("refund_id")
    String refundId;

    @Field("refunded_at")
    Instant refundedAt;

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

    public Integer getProductCode() {
        return productCode;
    }

    public void setProductCode(Integer productCode) {
        this.productCode = productCode;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public String getRefundId() {
        return refundId;
    }

    public void setRefundId(String refundId) {
        this.refundId = refundId;
    }

    public Instant getRefundedAt() {
        return refundedAt;
    }

    public void setRefundedAt(Instant refundedAt) {
        this.refundedAt = refundedAt;
    }
}
