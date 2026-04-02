package uk.gov.companieshouse.paymentreconciliation.consumer.service.handler;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.gov.companieshouse.api.model.payment.PaymentResponse;
import uk.gov.companieshouse.api.payments.PaymentDetailsResponse;
import uk.gov.companieshouse.paymentreconciliation.consumer.mapper.EshuMapper;
import uk.gov.companieshouse.paymentreconciliation.consumer.mapper.PaymentTransactionsResourceDaoMapper;
import uk.gov.companieshouse.paymentreconciliation.consumer.model.EshuDao;
import uk.gov.companieshouse.paymentreconciliation.consumer.model.PaymentTransactionsResourceDao;
import uk.gov.companieshouse.paymentreconciliation.consumer.repository.EshuRepository;
import uk.gov.companieshouse.paymentreconciliation.consumer.repository.TransactionRepository;

@ExtendWith(MockitoExtension.class)
class StandardTransactionHandlerTest {

    @Mock
    private EshuRepository eshuRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private EshuMapper eshuMapper;

    @Mock
    private PaymentTransactionsResourceDaoMapper paymentTransactionsResourceDaoMapper;

    @Mock
    private PaymentDetailsResponse paymentDetails;

    @Mock
    private PaymentResponse paymentResponse;

    @Mock
    private EshuDao eshuDao1;

    @Mock
    private EshuDao eshuDao2;

    @Mock
    private PaymentTransactionsResourceDao paymentTransactionsResourceDao;

    private StandardTransactionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new StandardTransactionHandler(eshuRepository, transactionRepository, eshuMapper,
                paymentTransactionsResourceDaoMapper);
    }

    @Test
    void handle_shouldMapAndSaveEshuAndPaymentTransactionsResources() {
        String paymentId = "PAY123";
        String transactionDateString = "2024-01-01T12:01:33Z";
        Instant transactionDate = Instant.parse(transactionDateString);
        String paymentStatus = "PAID";

        when(paymentResponse.getReference()).thenReturn(paymentId);
        when(paymentDetails.getTransactionDate()).thenReturn(transactionDateString);
        when(paymentDetails.getPaymentStatus()).thenReturn(paymentStatus);

        List<EshuDao> eshuList = Arrays.asList(eshuDao1, eshuDao2);
        List<PaymentTransactionsResourceDao> daoList = Arrays.asList(paymentTransactionsResourceDao);

        when(eshuMapper.mapFromPaymentResponse(paymentResponse, paymentId, transactionDate)).thenReturn(eshuList);
        when(paymentTransactionsResourceDaoMapper.mapFromPaymentResponse(paymentResponse, paymentId, transactionDate,
                paymentStatus)).thenReturn(daoList);

        // Act
        handler.handle(paymentDetails, paymentResponse);

        // Assert
        verify(eshuMapper).mapFromPaymentResponse(paymentResponse, paymentId, transactionDate);
        verify(eshuRepository).saveAll(eshuList);

        verify(paymentTransactionsResourceDaoMapper).mapFromPaymentResponse(paymentResponse, paymentId, transactionDate,
                paymentStatus);
        verify(transactionRepository).saveAll(daoList);
    }

    @Test
    void handle_shouldHandleEmptyListsGracefully() {
        String paymentId = "PAY456";
        String transactionDateString = "2024-01-01T12:01:33Z";
        Instant transactionDate = Instant.parse(transactionDateString);
        String paymentStatus = "FAILED";

        when(paymentResponse.getReference()).thenReturn(paymentId);
        when(paymentDetails.getTransactionDate()).thenReturn(transactionDateString);
        when(paymentDetails.getPaymentStatus()).thenReturn(paymentStatus);

        when(eshuMapper.mapFromPaymentResponse(paymentResponse, paymentId, transactionDate)).thenReturn(List.of());
        when(paymentTransactionsResourceDaoMapper.mapFromPaymentResponse(paymentResponse, paymentId, transactionDate,
                paymentStatus)).thenReturn(List.of());

        handler.handle(paymentDetails, paymentResponse);

        verify(eshuRepository).saveAll(List.of());
        verify(transactionRepository).saveAll(List.of());
    }
}