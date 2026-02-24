package uk.gov.companieshouse.paymentreconciliation.consumer.service.handler;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.retry.RetryException;

import payments.payment_processed;
import uk.gov.companieshouse.api.model.payment.PaymentResponse;
import uk.gov.companieshouse.api.model.payment.RefundModel;
import uk.gov.companieshouse.paymentreconciliation.consumer.apiclient.PaymentsApiClient;
import uk.gov.companieshouse.paymentreconciliation.consumer.mapper.RefundDaoMapper;
import uk.gov.companieshouse.paymentreconciliation.consumer.model.RefundDao;
import uk.gov.companieshouse.paymentreconciliation.consumer.repository.RefundRepository;


@ExtendWith(MockitoExtension.class)
class RefundTransactionHandlerTest {

    private RefundTransactionHandler handler;

    @Mock
    private PaymentsApiClient paymentsApiClient;

    @Mock
    private RefundRepository refundRepository;

    @Mock
    private RefundDaoMapper refundDaoMapper;

    @Mock
    private PaymentResponse paymentSession;

    @Mock
    private payment_processed paymentProcessed;

    @Mock
    private RefundModel refundMock;

    @Mock
    private RefundDao refundDao;


    @BeforeEach
    void setUp() {
        handler = new RefundTransactionHandler(paymentsApiClient, refundRepository, refundDaoMapper);
    }

    @Test
    void handle_refundStatusSubmitted_patchesAndRetriesIfStillSubmitted() {
        when(paymentProcessed.getRefundId()).thenReturn("refund123");
        when(paymentProcessed.getPaymentResourceId()).thenReturn("paymentId");
        when(paymentSession.getRefunds()).thenReturn(List.of(refundMock));
        when(refundMock.getRefundId()).thenReturn("refund123");
        when(refundMock.getStatus()).thenReturn("submitted");

        when(paymentsApiClient.patchLatestRefundStatus(anyString(), any(RefundModel.class))).thenReturn(refundMock);
        when(refundMock.getStatus()).thenReturn("submitted");

        assertThrows(RetryException.class, () -> handler.handle(paymentSession, paymentProcessed));
        verify(paymentsApiClient).patchLatestRefundStatus(anyString(), any(RefundModel.class));
    }

    @Test
    void handle_refundStatusSubmitted_patchesAndReconcilesIfSuccess() {
        when(paymentProcessed.getRefundId()).thenReturn("refund123");
        when(paymentProcessed.getPaymentResourceId()).thenReturn("paymentId");
        when(paymentSession.getRefunds()).thenReturn(List.of(refundMock));
        when(refundMock.getRefundId()).thenReturn("refund123");

        AtomicInteger callCount = new AtomicInteger(0);
        when(refundMock.getStatus()).thenAnswer(invocation -> {
            if (callCount.getAndIncrement() == 0) {
                return "submitted";
            } else {
                return "success";
            }
        });


        when(paymentsApiClient.patchLatestRefundStatus(anyString(), any(RefundModel.class))).thenReturn(refundMock);

        when(refundDaoMapper.mapFromRefund(anyString(), eq(paymentSession), eq(refundMock))).thenReturn(refundDao);

        handler.handle(paymentSession, paymentProcessed);

        verify(paymentsApiClient).patchLatestRefundStatus(anyString(), any(RefundModel.class));
        verify(refundDaoMapper).mapFromRefund("paymentId", paymentSession, refundMock);
        verify(refundRepository).save(refundDao);
    }

    @Test
    void handle_refundStatusSubmitted_patchesAndSkipsIfFailed() {
        when(paymentProcessed.getRefundId()).thenReturn("refund123");
        when(paymentProcessed.getPaymentResourceId()).thenReturn("paymentId");

        when(paymentSession.getRefunds()).thenReturn(List.of(refundMock));
        when(refundMock.getRefundId()).thenReturn("refund123");

        AtomicInteger callCount = new AtomicInteger(0);
        when(refundMock.getStatus()).thenAnswer(invocation -> {
            if (callCount.getAndIncrement() == 0) {
                return "submitted";
            } else {
                return "failed";
            }
        });

        when(paymentsApiClient.patchLatestRefundStatus(anyString(), any(RefundModel.class))).thenReturn(refundMock);

        handler.handle(paymentSession, paymentProcessed);

        verify(paymentsApiClient).patchLatestRefundStatus(anyString(), any(RefundModel.class));
        verifyNoInteractions(refundDaoMapper);
        verifyNoInteractions(refundRepository);
    }

    @Test
    void handle_refundStatusSuccess_reconciles() {
        when(paymentProcessed.getRefundId()).thenReturn("refund123");
        when(paymentProcessed.getPaymentResourceId()).thenReturn("paymentId");
        when(paymentSession.getRefunds()).thenReturn(List.of(refundMock));
        when(refundMock.getRefundId()).thenReturn("refund123");
        when(refundMock.getStatus()).thenReturn("success");

        when(refundDaoMapper.mapFromRefund(anyString(), eq(paymentSession), eq(refundMock))).thenReturn(refundDao);

        handler.handle(paymentSession, paymentProcessed);

        verify(refundDaoMapper).mapFromRefund("paymentId", paymentSession, refundMock);
        verify(refundRepository).save(refundDao);
        verifyNoInteractions(paymentsApiClient);
    }

    @Test
    void handle_refundStatusRefundSuccess_reconciles() {
        when(paymentProcessed.getRefundId()).thenReturn("refund123");
        when(paymentProcessed.getPaymentResourceId()).thenReturn("paymentId");
        when(paymentSession.getRefunds()).thenReturn(List.of(refundMock));
        when(refundMock.getRefundId()).thenReturn("refund123");
        when(refundMock.getStatus()).thenReturn("refund-success");

        when(refundDaoMapper.mapFromRefund(anyString(), eq(paymentSession), eq(refundMock))).thenReturn(refundDao);

        handler.handle(paymentSession, paymentProcessed);

        verify(refundDaoMapper).mapFromRefund("paymentId", paymentSession, refundMock);
        verify(refundRepository).save(refundDao);
        verifyNoInteractions(paymentsApiClient);
    }

    @Test
    void handle_refundStatusFailed_skipsReconciliation() {
        when(paymentProcessed.getRefundId()).thenReturn("refund123");
        when(paymentSession.getRefunds()).thenReturn(List.of(refundMock));
        when(refundMock.getRefundId()).thenReturn("refund123");
        when(refundMock.getStatus()).thenReturn("failed");

        handler.handle(paymentSession, paymentProcessed);

        verifyNoInteractions(refundDaoMapper);
        verifyNoInteractions(refundRepository);
        verifyNoInteractions(paymentsApiClient);
    }

    @Test
    void handle_refundIsNull_doesNothing() {

        when(paymentProcessed.getRefundId()).thenReturn("refund123");
        when(paymentSession.getRefunds()).thenReturn(List.of());

        handler.handle(paymentSession, paymentProcessed);

        verifyNoInteractions(refundDaoMapper);
        verifyNoInteractions(refundRepository);
        verifyNoInteractions(paymentsApiClient);
    }

    @Test
    void handle_refundIdIsNull_doesNothing() {
        when(paymentProcessed.getRefundId()).thenReturn(null);

        handler.handle(paymentSession, paymentProcessed);

        verifyNoInteractions(refundDaoMapper);
        verifyNoInteractions(refundRepository);
        verifyNoInteractions(paymentsApiClient);
    }
}