package uk.gov.companieshouse.paymentreconciliation.consumer.service.handler;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.retry.RetryException;

import payments.payment_processed;
import uk.gov.companieshouse.api.model.payment.PaymentResponse;
import uk.gov.companieshouse.api.model.payment.RefundModel;
import uk.gov.companieshouse.api.payments.Refund;
import uk.gov.companieshouse.paymentreconciliation.consumer.apiclient.PaymentsApiClient;
import uk.gov.companieshouse.paymentreconciliation.consumer.mapper.RefundDaoMapper;
import uk.gov.companieshouse.paymentreconciliation.consumer.model.RefundDao;
import uk.gov.companieshouse.paymentreconciliation.consumer.repository.RefundRepository;

class RefundTransactionHandlerTest {

    private PaymentsApiClient paymentsApiClient;
    private RefundRepository refundRepository;
    private RefundDaoMapper refundDaoMapper;
    private RefundTransactionHandler handler;

    @BeforeEach
    void setUp() {
        paymentsApiClient = mock(PaymentsApiClient.class);
        refundRepository = mock(RefundRepository.class);
        refundDaoMapper = mock(RefundDaoMapper.class);
        handler = new RefundTransactionHandler(paymentsApiClient, refundRepository, refundDaoMapper);
    }

    @Test
    void handle_refundStatusSubmitted_patchesAndRetriesIfStillSubmitted() {
        PaymentResponse paymentSession = mock(PaymentResponse.class);
        payment_processed paymentProcessed = mock(payment_processed.class);
        RefundModel refund = new RefundModel();
        refund.setRefundId("refund123");
        refund.setStatus("submitted");
        when(paymentProcessed.getRefundId()).thenReturn("refund123");
        when(paymentProcessed.getPaymentResourceId()).thenReturn("paymentId");
        when(paymentSession.getRefunds()).thenReturn(List.of(refund));
        RefundModel patchedRefund = mock(RefundModel.class);
        when(paymentsApiClient.patchLatestRefundStatus(anyString(), any(RefundModel.class))).thenReturn(patchedRefund);
        when(patchedRefund.getStatus()).thenReturn("submitted");

        assertThrows(RetryException.class, () -> handler.handle(paymentSession, paymentProcessed));
        verify(paymentsApiClient).patchLatestRefundStatus(anyString(), any(RefundModel.class));
    }

    @Test
    void handle_refundStatusSubmitted_patchesAndReconcilesIfSuccess() {
        PaymentResponse paymentSession = mock(PaymentResponse.class);
        payment_processed paymentProcessed = mock(payment_processed.class);
        RefundModel refund = mock(RefundModel.class);

        when(paymentProcessed.getRefundId()).thenReturn("refund123");
        when(paymentProcessed.getPaymentResourceId()).thenReturn("paymentId");
        when(paymentSession.getRefunds()).thenReturn(List.of(refund));
        when(refund.getRefundId()).thenReturn("refund123");
        when(refund.getStatus()).thenReturn("submitted");
        RefundModel patchedRefund = mock(RefundModel.class);
        when(paymentsApiClient.patchLatestRefundStatus(anyString(), any(RefundModel.class))).thenReturn(patchedRefund);
        when(patchedRefund.getStatus()).thenReturn("success");

        RefundDao refundDao = mock(RefundDao.class);
        when(refundDaoMapper.mapFromRefund(anyString(), eq(paymentSession), eq(patchedRefund))).thenReturn(refundDao);

        handler.handle(paymentSession, paymentProcessed);

        verify(paymentsApiClient).patchLatestRefundStatus(anyString(), any(RefundModel.class));
        verify(refundDaoMapper).mapFromRefund("paymentId", paymentSession, patchedRefund);
        verify(refundRepository).save(refundDao);
    }

    @Test
    void handle_refundStatusSubmitted_patchesAndSkipsIfFailed() {
        PaymentResponse paymentSession = mock(PaymentResponse.class);
        payment_processed paymentProcessed = new payment_processed();
        paymentProcessed.setRefundId("refund123");
        paymentProcessed.setPaymentResourceId("paymentId");
        RefundModel refund = mock(RefundModel.class);

        when(paymentSession.getRefunds()).thenReturn(List.of(refund));
        when(refund.getRefundId()).thenReturn("refund123");
        when(refund.getStatus()).thenReturn("submitted");
        RefundModel patchedRefund = mock(RefundModel.class);
        when(paymentsApiClient.patchLatestRefundStatus(anyString(), any(RefundModel.class))).thenReturn(patchedRefund);
        when(patchedRefund.getStatus()).thenReturn("failed");

        handler.handle(paymentSession, paymentProcessed);

        verify(paymentsApiClient).patchLatestRefundStatus(anyString(), any(RefundModel.class));
        verifyNoInteractions(refundDaoMapper);
        verifyNoInteractions(refundRepository);
    }

    @Test
    void handle_refundStatusSuccess_reconciles() {
        PaymentResponse paymentSession = mock(PaymentResponse.class);
        payment_processed paymentProcessed = mock(payment_processed.class);
        RefundModel refund = mock(RefundModel.class);
        when(paymentProcessed.getRefundId()).thenReturn("refund123");
        when(paymentProcessed.getPaymentResourceId()).thenReturn("paymentId");
        when(paymentSession.getRefunds()).thenReturn(List.of(refund));
        when(refund.getRefundId()).thenReturn("refund123");
        when(refund.getStatus()).thenReturn("success");

        RefundDao refundDao = mock(RefundDao.class);
        when(refundDaoMapper.mapFromRefund(anyString(), eq(paymentSession), eq(refund))).thenReturn(refundDao);

        handler.handle(paymentSession, paymentProcessed);

        verify(refundDaoMapper).mapFromRefund("paymentId", paymentSession, refund);
        verify(refundRepository).save(refundDao);
        verifyNoInteractions(paymentsApiClient);
    }

    @Test
    void handle_refundStatusRefundSuccess_reconciles() {
        PaymentResponse paymentSession = mock(PaymentResponse.class);
        payment_processed paymentProcessed = mock(payment_processed.class);
        RefundModel refund = mock(RefundModel.class);

        when(paymentProcessed.getRefundId()).thenReturn("refund123");
        when(paymentProcessed.getPaymentResourceId()).thenReturn("paymentId");
        when(paymentSession.getRefunds()).thenReturn(List.of(refund));
        when(refund.getRefundId()).thenReturn("refund123");
        when(refund.getStatus()).thenReturn("refund-success");

        RefundDao refundDao = mock(RefundDao.class);
        when(refundDaoMapper.mapFromRefund(anyString(), eq(paymentSession), eq(refund))).thenReturn(refundDao);

        handler.handle(paymentSession, paymentProcessed);

        verify(refundDaoMapper).mapFromRefund("paymentId", paymentSession, refund);
        verify(refundRepository).save(refundDao);
        verifyNoInteractions(paymentsApiClient);
    }

    @Test
    void handle_refundStatusFailed_skipsReconciliation() {
        PaymentResponse paymentSession = mock(PaymentResponse.class);
        payment_processed paymentProcessed = mock(payment_processed.class);
        RefundModel refund = mock(RefundModel.class);

        when(paymentProcessed.getRefundId()).thenReturn("refund123");
        when(paymentSession.getRefunds()).thenReturn(List.of(refund));
        when(refund.getRefundId()).thenReturn("refund123");
        when(refund.getStatus()).thenReturn("failed");

        handler.handle(paymentSession, paymentProcessed);

        verifyNoInteractions(refundDaoMapper);
        verifyNoInteractions(refundRepository);
        verifyNoInteractions(paymentsApiClient);
    }

    @Test
    void handle_refundIsNull_doesNothing() {
        PaymentResponse paymentSession = mock(PaymentResponse.class);
        payment_processed paymentProcessed = mock(payment_processed.class);

        when(paymentProcessed.getRefundId()).thenReturn("refund123");
        when(paymentSession.getRefunds()).thenReturn(List.of());

        handler.handle(paymentSession, paymentProcessed);

        verifyNoInteractions(refundDaoMapper);
        verifyNoInteractions(refundRepository);
        verifyNoInteractions(paymentsApiClient);
    }

    @Test
    void handle_refundIdIsNull_doesNothing() {
        PaymentResponse paymentSession = mock(PaymentResponse.class);
        payment_processed paymentProcessed = mock(payment_processed.class);

        when(paymentProcessed.getRefundId()).thenReturn(null);

        handler.handle(paymentSession, paymentProcessed);

        verifyNoInteractions(refundDaoMapper);
        verifyNoInteractions(refundRepository);
        verifyNoInteractions(paymentsApiClient);
    }
}